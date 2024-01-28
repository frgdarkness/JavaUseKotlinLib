package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

enum class AISchedule(val value: String) {
    DDPM_SCHEDULER("DDPMScheduler"),
    DDIM_SCHEDULER("DDIMScheduler"),
    PNDM_SCHEDULER("PNDMScheduler"),
    LMS_DISCRETE_SCHEDULER("LMSDiscreteScheduler"),
    EULER_DISCRETE_SCHEDULER("EulerDiscreteScheduler"),
    EULER_ANCESTRAL_DISCRETE_SCHEDULER("EulerAncestralDiscreteScheduler"),
    UNI_PC_MULTI_STEP_SCHEDULER("UniPCMultistepScheduler"),
    DPM_SOLVER_MULTI_STEP_SCHEDULER("DPMSolverMultistepScheduler"),
    HEUN_DISCRETE_SCHEDULER("HeunDiscreteScheduler"),
}