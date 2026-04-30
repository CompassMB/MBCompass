// SPDX-License-Identifier: GPL-3.0-or-later



package com.mubarak.mbcompass.features.tracks

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.core.location.LocationHelper
import com.mubarak.mbcompass.features.tracks.model.Track
import com.mubarak.mbcompass.utils.DateTimeFormatter
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polyline
import java.text.DecimalFormat


class MapOverlayHelper {


    fun createMyLocationOverlay(
        context: Context,
        location: Location,
        trackingState: Int
    ): ItemizedIconOverlay<OverlayItem> {

        val overlayItems = ArrayList<OverlayItem>()
        val locationIsOld = LocationHelper.isStaleLocation(location)

        // Select marker based on tracking state AND location age
        val marker: Drawable = when (trackingState) {
            TrackingConstants.STATE_TRACKING_ACTIVE -> {
                // Red marker when actively tracking
                if (locationIsOld) {
                    ContextCompat.getDrawable(context, R.drawable.marker_location_old_tracking)!!
                } else {
                    ContextCompat.getDrawable(context, R.drawable.marker_location_tracking)!!
                }
            }
            else -> {
                // Blue marker when not tracking
                if (locationIsOld) {
                    ContextCompat.getDrawable(context, R.drawable.marker_location_old)!!
                } else {
                    ContextCompat.getDrawable(context, R.drawable.current_location_24px)!!
                }
            }
        }

        val overlayItem = createOverlayItem(
            context,
            location.latitude,
            location.longitude,
            location.accuracy,
            location.provider.toString(),
            location.time
        )
        overlayItem.setMarker(marker)
        overlayItems.add(overlayItem)

        return createSimpleOverlay(context, overlayItems)
    }


    fun createTrackOverlay(
        context: Context,
        track: Track,
        trackingState: Int
    ): Polyline {
        // Color based on tracking state
        val color = if (trackingState == TrackingConstants.STATE_TRACKING_ACTIVE) {
            ContextCompat.getColor(context, R.color.attr_color)  // Red when tracking
        } else {
            ContextCompat.getColor(context, R.color.brand_color)  // Blue when saved
        }

        // Convert waypoints to GeoPoints
        val points: MutableList<GeoPoint> = mutableListOf()
        track.wayPoints.forEach { wayPoint ->
            points.add(GeoPoint(wayPoint.latitude, wayPoint.longitude, wayPoint.altitude))
        }

        // Create polyline with styling
        val overlay = Polyline()
        overlay.outlinePaint.color = color
        overlay.outlinePaint.strokeCap = Paint.Cap.ROUND
        overlay.outlinePaint.strokeJoin = Paint.Join.ROUND
        overlay.setPoints(points)

        return overlay
    }


    fun createStartEndMarkersOverlay(
        context: Context,
        track: Track,
        displayMarkers: Boolean = false
    ): ItemizedIconOverlay<OverlayItem> {

        val overlayItems = ArrayList<OverlayItem>()

        // Only show markers if explicitly requested (for saved tracks)
        if (!displayMarkers || track.wayPoints.isEmpty()) {
            return createSimpleOverlay(context, overlayItems)
        }

        val maxIndex = track.wayPoints.size - 1

        // Start marker (first waypoint)
        val startWaypoint = track.wayPoints[0]
        val startItem = createOverlayItem(
            context,
            startWaypoint.latitude,
            startWaypoint.longitude,
            startWaypoint.accuracy,
            startWaypoint.locationProvider,
            startWaypoint.time
        )
        startItem.setMarker(
            ContextCompat.getDrawable(context, R.drawable.marker_start_48dp)!!
        )
        overlayItems.add(startItem)

        // End marker (last waypoint) - only if different from start
        if (maxIndex > 0) {
            val endWaypoint = track.wayPoints[maxIndex]
            val endItem = createOverlayItem(
                context,
                endWaypoint.latitude,
                endWaypoint.longitude,
                endWaypoint.accuracy,
                endWaypoint.locationProvider,
                endWaypoint.time
            )
            endItem.setMarker(
                ContextCompat.getDrawable(context, R.drawable.marker_end_48dp)!!
            )
            overlayItems.add(endItem)
        }

        return createSimpleOverlay(context, overlayItems)
    }

    private fun createOverlayItem(
        context: Context,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        provider: String,
        time: Long
    ): OverlayItem {
        val title = DateTimeFormatter.formatDateTimeString(time)
        val description = "${context.getString(R.string.tracks)}: " +
                "${DecimalFormat("#0.00").format(accuracy)}m ($provider)"
        val position = GeoPoint(latitude, longitude)
        val item = OverlayItem(title, description, position)
        item.markerHotspot = OverlayItem.HotspotPlace.CENTER
        return item
    }

    private fun createSimpleOverlay(
        context: Context,
        overlayItems: ArrayList<OverlayItem>
    ): ItemizedIconOverlay<OverlayItem> {
        return ItemizedIconOverlay(
            context,
            overlayItems,
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                    return false
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                    return false
                }
            }
        )
    }
}