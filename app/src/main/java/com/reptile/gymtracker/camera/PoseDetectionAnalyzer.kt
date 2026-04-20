package com.reptile.gymtracker.camera

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.camera.model.LandmarkType
import com.reptile.gymtracker.camera.model.PoseLandmarkPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoseDetectionAnalyzer @Inject constructor(
    private val poseDetector: PoseDetector
) : ImageAnalysis.Analyzer {

    private val _poseFlow = MutableStateFlow<DetectedPose?>(null)
    val poseFlow: StateFlow<DetectedPose?> = _poseFlow

    private val _imageSizeFlow = MutableStateFlow<Pair<Int, Int>?>(null)
    val imageSizeFlow: StateFlow<Pair<Int, Int>?> = _imageSizeFlow

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        _imageSizeFlow.value = imageProxy.width to imageProxy.height

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        poseDetector.process(inputImage)
            .addOnSuccessListener { pose ->
                val allLandmarks = pose.allPoseLandmarks
                if (allLandmarks.isEmpty()) {
                    _poseFlow.value = null
                } else {
                    val landmarkMap = mutableMapOf<LandmarkType, PoseLandmarkPoint>()
                    for (landmark in allLandmarks) {
                        val type = LandmarkType.fromMlKit(landmark.landmarkType) ?: continue
                        landmarkMap[type] = PoseLandmarkPoint(
                            x = landmark.position.x / imageProxy.width,
                            y = landmark.position.y / imageProxy.height,
                            z = landmark.position3D.z,
                            confidence = landmark.inFrameLikelihood
                        )
                    }
                    val avgConfidence = allLandmarks
                        .map { it.inFrameLikelihood }
                        .average()
                        .toFloat()
                    _poseFlow.value = DetectedPose(
                        landmarks = landmarkMap,
                        timestampMs = System.currentTimeMillis(),
                        confidence = avgConfidence
                    )
                }
            }
            .addOnFailureListener {
                _poseFlow.value = null
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun close() {
        poseDetector.close()
    }
}
