package kz.petprojects.facerecognitionapp

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark

@Composable
fun FaceContourOverlay(
    faces: List<Face>,
    showGlasses: Boolean
) {

    Canvas(
        modifier = Modifier,
    ) {

        for (face in faces) {
            if (!showGlasses) {

                // Adjust the bounding box coordinates
                val bounds = face.boundingBox
                val left = bounds.left * SCALE - OFFSET_X
                val top = bounds.top * SCALE - OFFSET_Y
                val right = bounds.right * SCALE - OFFSET_X
                val bottom = bounds.bottom * SCALE - OFFSET_Y

                // Draw bounding box (frame) around the face
                drawRect(
                    color = Color.Blue,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = 4f)
                )

                // Draw contours adjusted for scale
                for (contour in face.allContours) {
                    for (i in 0 until contour.points.size - 1) {
                        val startPoint = contour.points[i]
                        val endPoint = contour.points[i + 1]

                        Log.e("coordinates without scaling", "${startPoint.x} ${startPoint.y}")

                        val startX = startPoint.x * SCALE - OFFSET_X
                        val startY = startPoint.y * SCALE - OFFSET_Y
                        val endX = endPoint.x * SCALE - OFFSET_X
                        val endY = endPoint.y * SCALE - OFFSET_Y

                        Log.e("coordinates after scaling", "$startX $startY")
                        // Draw line connecting points
                        drawLine(
                            color = Color.Red,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 2f
                        )
                    }

                    // Optional: Draw individual points
                    for (point in contour.points) {
                        val pointX = point.x * SCALE - OFFSET_X
                        val pointY = point.y * SCALE - OFFSET_Y

                        drawCircle(
                            color = Color.Green,
                            radius = 3f,
                            center = Offset(pointX, pointY)
                        )
                    }

                }
            } else {
                drawGlasses(face)
            }
        }
    }
}


private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGlasses(
    face: Face,
) {

    // Get eye landmarks
    val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
    val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position

    if (leftEye != null && rightEye != null) {
        // Scale eye positions
        val leftEyeX = leftEye.x * SCALE - OFFSET_X
        val leftEyeY = leftEye.y * SCALE - OFFSET_Y
        val rightEyeX = rightEye.x * SCALE - OFFSET_X
        val rightEyeY = rightEye.y * SCALE - OFFSET_Y

        // Define glasses properties
        val lensWidth = (rightEyeX - leftEyeX) * 0.5f
        val lensHeight = lensWidth * 0.6f

        // Draw left lens
        drawRect(
            color = Color.Black,
            topLeft = Offset(leftEyeX - lensWidth / 2, leftEyeY - lensHeight / 2),
            size = Size(lensWidth, lensHeight),
            style = Stroke(width = 3f)
        )

        // Draw right lens
        drawRect(
            color = Color.Black,
            topLeft = Offset(rightEyeX - lensWidth / 2, rightEyeY - lensHeight / 2),
            size = Size(lensWidth, lensHeight),
            style = Stroke(width = 3f)
        )

        // Draw bridge
        drawLine(
            color = Color.Black,
            start = Offset(leftEyeX + lensWidth / 2, leftEyeY),
            end = Offset(rightEyeX - lensWidth / 2, rightEyeY),
            strokeWidth = 3f
        )
    }
}

const val SCALE = 3.8.toFloat()
const val OFFSET_X = 370
const val OFFSET_Y = 40
