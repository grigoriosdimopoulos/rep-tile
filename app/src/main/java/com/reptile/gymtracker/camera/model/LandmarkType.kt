package com.reptile.gymtracker.camera.model

import com.google.mlkit.vision.pose.PoseLandmark

enum class LandmarkType(val mlKitType: Int) {
    NOSE(PoseLandmark.NOSE),
    LEFT_EYE_INNER(PoseLandmark.LEFT_EYE_INNER),
    LEFT_EYE(PoseLandmark.LEFT_EYE),
    LEFT_EYE_OUTER(PoseLandmark.LEFT_EYE_OUTER),
    RIGHT_EYE_INNER(PoseLandmark.RIGHT_EYE_INNER),
    RIGHT_EYE(PoseLandmark.RIGHT_EYE),
    RIGHT_EYE_OUTER(PoseLandmark.RIGHT_EYE_OUTER),
    LEFT_EAR(PoseLandmark.LEFT_EAR),
    RIGHT_EAR(PoseLandmark.RIGHT_EAR),
    LEFT_MOUTH(PoseLandmark.LEFT_MOUTH),
    RIGHT_MOUTH(PoseLandmark.RIGHT_MOUTH),
    LEFT_SHOULDER(PoseLandmark.LEFT_SHOULDER),
    RIGHT_SHOULDER(PoseLandmark.RIGHT_SHOULDER),
    LEFT_ELBOW(PoseLandmark.LEFT_ELBOW),
    RIGHT_ELBOW(PoseLandmark.RIGHT_ELBOW),
    LEFT_WRIST(PoseLandmark.LEFT_WRIST),
    RIGHT_WRIST(PoseLandmark.RIGHT_WRIST),
    LEFT_PINKY(PoseLandmark.LEFT_PINKY),
    RIGHT_PINKY(PoseLandmark.RIGHT_PINKY),
    LEFT_INDEX(PoseLandmark.LEFT_INDEX),
    RIGHT_INDEX(PoseLandmark.RIGHT_INDEX),
    LEFT_THUMB(PoseLandmark.LEFT_THUMB),
    RIGHT_THUMB(PoseLandmark.RIGHT_THUMB),
    LEFT_HIP(PoseLandmark.LEFT_HIP),
    RIGHT_HIP(PoseLandmark.RIGHT_HIP),
    LEFT_KNEE(PoseLandmark.LEFT_KNEE),
    RIGHT_KNEE(PoseLandmark.RIGHT_KNEE),
    LEFT_ANKLE(PoseLandmark.LEFT_ANKLE),
    RIGHT_ANKLE(PoseLandmark.RIGHT_ANKLE),
    LEFT_HEEL(PoseLandmark.LEFT_HEEL),
    RIGHT_HEEL(PoseLandmark.RIGHT_HEEL),
    LEFT_FOOT_INDEX(PoseLandmark.LEFT_FOOT_INDEX),
    RIGHT_FOOT_INDEX(PoseLandmark.RIGHT_FOOT_INDEX);

    companion object {
        private val byMlKit = entries.associateBy { it.mlKitType }
        fun fromMlKit(type: Int): LandmarkType? = byMlKit[type]
    }
}

val SKELETON_CONNECTIONS = listOf(
    LandmarkType.LEFT_SHOULDER to LandmarkType.RIGHT_SHOULDER,
    LandmarkType.LEFT_SHOULDER to LandmarkType.LEFT_ELBOW,
    LandmarkType.LEFT_ELBOW to LandmarkType.LEFT_WRIST,
    LandmarkType.RIGHT_SHOULDER to LandmarkType.RIGHT_ELBOW,
    LandmarkType.RIGHT_ELBOW to LandmarkType.RIGHT_WRIST,
    LandmarkType.LEFT_SHOULDER to LandmarkType.LEFT_HIP,
    LandmarkType.RIGHT_SHOULDER to LandmarkType.RIGHT_HIP,
    LandmarkType.LEFT_HIP to LandmarkType.RIGHT_HIP,
    LandmarkType.LEFT_HIP to LandmarkType.LEFT_KNEE,
    LandmarkType.LEFT_KNEE to LandmarkType.LEFT_ANKLE,
    LandmarkType.RIGHT_HIP to LandmarkType.RIGHT_KNEE,
    LandmarkType.RIGHT_KNEE to LandmarkType.RIGHT_ANKLE,
    LandmarkType.LEFT_ANKLE to LandmarkType.LEFT_HEEL,
    LandmarkType.RIGHT_ANKLE to LandmarkType.RIGHT_HEEL
)
