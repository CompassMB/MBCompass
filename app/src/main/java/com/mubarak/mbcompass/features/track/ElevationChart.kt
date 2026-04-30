// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.features.track

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mubarak.mbcompass.features.tracks.model.WayPoint
import kotlin.math.*

@Composable
fun ElevationChart(
    waypoints: List<WayPoint>,
    modifier: Modifier = Modifier,
    useMetric: Boolean = true
) {
    val validPoints = remember(waypoints) {
        waypoints.filter { it.altitude != 0.0 }
    }

    if (validPoints.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "No elevation data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val elevations = remember(validPoints) {
        validPoints.map { it.altitude.toFloat() }
    }

    val distances = remember(validPoints) {
        var cum = 0f
        val list = mutableListOf(0f)
        for (i in 1 until validPoints.size) {
            cum += calcDistance(validPoints[i - 1], validPoints[i])
            list.add(cum)
        }
        list
    }

    val rawMin = elevations.min()
    val rawMax = elevations.max()
    val rawRange = (rawMax - rawMin).coerceAtLeast(1f)
    val pad = rawRange * 0.15f
    val minE = rawMin - pad
    val maxE = rawMax + pad
    val eRange = maxE - minE

    val totalDist = distances.last().coerceAtLeast(1f)

    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val leftPadDp  = 44.dp
    val bottomPadDp = 20.dp
    val topPadDp   = 8.dp

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current

        val lp = with(density) { leftPadDp.toPx() }
        val bp = with(density) { bottomPadDp.toPx() }
        val tp = with(density) { topPadDp.toPx() }

        val totalW = with(density) { maxWidth.toPx() }
        val totalH = with(density) { maxHeight.toPx() }

        val chartW = totalW - lp
        val chartH = totalH - bp - tp

        fun x(d: Float) = lp + (d / totalDist) * chartW
        fun y(e: Float) = tp + chartH - ((e - minE) / eRange) * chartH

        Canvas(modifier = Modifier.fillMaxSize()) {

            val pts = elevations.indices.map { i ->
                Offset(x(distances[i]), y(elevations[i]))
            }

            val splinePath = catmullRomToBezier(pts)

            // fill path
            val fillPath = Path().apply {
                addPath(splinePath)
                lineTo(pts.last().x, tp + chartH)
                lineTo(pts.first().x, tp + chartH)
                close()
            }

            // gradient fil
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to primaryColor.copy(alpha = 0.25f),
                        0.6f to primaryColor.copy(alpha = 0.10f),
                        1.0f to primaryColor.copy(alpha = 0.0f)
                    ),
                    startY = tp,
                    endY = tp + chartH
                )
            )

            drawPath(
                path = splinePath,
                color = primaryColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // baseline
            drawLine(
                color = primaryColor.copy(alpha = 0.15f),
                start = Offset(lp, tp + chartH),
                end   = Offset(totalW, tp + chartH),
                strokeWidth = 1.dp.toPx()
            )

            listOf(pts.first(), pts.last()).forEach { p ->
                drawCircle(color = primaryColor, radius = 3.dp.toPx(), center = p)
                drawCircle(
                    color = Color.White,
                    radius = 1.5.dp.toPx(),
                    center = p
                )
            }
        }

        // Yaxis labels
        fun formatAlt(v: Float) = if (useMetric) "%.0f m".format(v)
        else "%.0f ft".format(v * 3.28084f)

        Text(
            text = formatAlt(rawMax),
            fontSize = 9.sp,
            color = labelColor,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = topPadDp, start = 2.dp)
        )
        Text(
            text = formatAlt(rawMin),
            fontSize = 9.sp,
            color = labelColor,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = bottomPadDp + 4.dp, start = 2.dp)
        )

        // Xaxis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(start = leftPadDp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val steps = 3
            repeat(steps + 1) { i ->
                val d = totalDist * i / steps
                val label = if (useMetric) {
                    if (d < 1000f) "%.0f m".format(d)
                    else "%.1f km".format(d / 1000f)
                } else {
                    "%.2f mi".format(d / 1609.34f)
                }
                Text(text = label, fontSize = 9.sp, color = labelColor)
            }
        }
    }
}


// convert it to bezier https://cubic-bezier.com/ for smooth spine
private fun catmullRomToBezier(pts: List<Offset>): Path {
    val path = Path()
    if (pts.size < 2) return path

    path.moveTo(pts[0].x, pts[0].y)

    for (i in 0 until pts.size - 1) {
        val p0 = if (i == 0) pts[0] else pts[i - 1]
        val p1 = pts[i]
        val p2 = pts[i + 1]
        val p3 = if (i + 2 < pts.size) pts[i + 2] else pts[i + 1]

        val cp1x = p1.x + (p2.x - p0.x) / 6f
        val cp1y = p1.y + (p2.y - p0.y) / 6f
        val cp2x = p2.x - (p3.x - p1.x) / 6f
        val cp2y = p2.y - (p3.y - p1.y) / 6f

        path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
    }

    return path
}

// calculate distance using Haversine formula
private fun calcDistance(p1: WayPoint, p2: WayPoint): Float {
    val R = 6_371_000f
    val lat1 = Math.toRadians(p1.latitude)
    val lat2 = Math.toRadians(p2.latitude)
    val dLat = Math.toRadians(p2.latitude - p1.latitude)
    val dLon = Math.toRadians(p2.longitude - p1.longitude)

    val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    return (R * 2 * atan2(sqrt(a), sqrt(1 - a))).toFloat()
}