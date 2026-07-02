// SPDX-License-Identifier: GPL-3.0-or-later
/*
 * Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
 * This project is licensed under GPL-3.0. Any derivative work must keep the same license,
 * retain this copyright notice, and provide proper attribution.
 */

package com.mubarak.mbcompass

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import org.mapsforge.map.rendertheme.InternalRenderTheme
import org.osmdroid.mapsforge.MapsForgeTileProvider
import org.osmdroid.mapsforge.MapsForgeTileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.views.MapView
import java.io.FileInputStream

private const val TAG = "MapProvider"

object MapProvider {


    // returns all .map files inside folder uri stored in preferences
    fun getOnDeviceMapFiles(context: Context, folderUri: String): List<DocumentFile> {
        if (folderUri.isEmpty()) return emptyList()
        val folder = DocumentFile.fromTreeUri(context, folderUri.toUri()) ?: return emptyList()
        return folder.listFiles().filter { it.name?.endsWith(".map") == true }
    }

    fun getOnDeviceMapFolderName(context: Context, folderUri: String): String {
        if (folderUri.isEmpty()) return ""
        return DocumentFile.fromTreeUri(context, folderUri.toUri())?.name ?: ""
    }


    fun makeUriPersistent(context: Context, uri: Uri) {
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flags)
    }

    private fun getMapFileInputStreams(
        context: Context,
        mapFiles: List<DocumentFile>
    ): Array<FileInputStream> {
        return mapFiles.mapNotNull { doc ->
            try {
                context.contentResolver.openInputStream(doc.uri) as? FileInputStream
            } catch (e: Exception) {
                Log.e(TAG, "Could not open stream for ${doc.name}", e)
                null
            }
        }.toTypedArray()
    }


    fun getOfflineMapTileProvider(
        context: Context,
        mapFiles: List<DocumentFile>
    ): MapsForgeTileProvider? {
        if (mapFiles.isEmpty()) return null
        return try {
            val streams = getMapFileInputStreams(context, mapFiles)
            if (streams.isEmpty()) return null

            val tileSource = MapsForgeTileSource.createFromFileInputStream(
                streams,
                InternalRenderTheme.OSMARENDER,
                InternalRenderTheme.OSMARENDER.name
            )
            MapsForgeTileProvider(SimpleRegisterReceiver(context), tileSource, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create offline tile provider", e)
            null
        }
    }

    fun applyMapSource(
        context: Context,
        mapView: MapView,
        useOfflineMaps: Boolean,
        offlineMapFolder: String,
    ) {
        if (useOfflineMaps) {
            val mapFiles = getOnDeviceMapFiles(context, offlineMapFolder)
            val provider = getOfflineMapTileProvider(context, mapFiles)
            if (provider != null) {
                mapView.setTileProvider(provider)
                mapView.isTilesScaledToDpi = false
                Log.d(TAG, "Offline map provider set (${mapFiles.size} .map files)")
            } else {
                // fallback to online source
                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.isTilesScaledToDpi = true
                Log.w(TAG, "No .map files found, falling back to MAPNIK")
            }
        } else {
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.isTilesScaledToDpi = true
            Log.d(TAG, "Online MAPNIK tile source set")
        }
        mapView.invalidate()
    }
}