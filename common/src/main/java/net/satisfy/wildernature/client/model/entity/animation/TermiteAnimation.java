package net.satisfy.wildernature.client.model.entity.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class TermiteAnimation {
    public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength(1.35F).looping()
            .addAnimation("torso", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.15F, KeyframeAnimations.degreeVec(1.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.45F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.6F, KeyframeAnimations.degreeVec(1.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.05F, KeyframeAnimations.degreeVec(1.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.35F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("torso", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -0.05F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, -0.05F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.15F, KeyframeAnimations.posVec(0.0F, -0.05F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.35F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("antennae", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.35F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.45F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.55F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.35F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();

    public static final AnimationDefinition walk = AnimationDefinition.Builder.withLength(0.3F).looping()
            .addAnimation("base", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("base", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.15F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.25F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("booty", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("booty", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("torso", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.15F, KeyframeAnimations.degreeVec(1.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("torso", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1F, KeyframeAnimations.posVec(-0.0087F, -0.0996F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("antennae", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.15F, KeyframeAnimations.degreeVec(-12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("antennae", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-1.7538F, -9.8466F, 20.1511F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1F, KeyframeAnimations.degreeVec(1.2446F, -6.4932F, -0.1834F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(-2.3162F, 13.1915F, -0.3946F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(-1.7538F, -9.8466F, 20.1511F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(1.2446F, -6.4932F, -0.1834F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1F, KeyframeAnimations.degreeVec(-2.3162F, 13.1915F, -0.3946F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(-1.7538F, -9.8466F, 20.1511F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(1.2446F, -6.4932F, -0.1834F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(1.2446F, 6.4932F, 0.1834F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1F, KeyframeAnimations.degreeVec(-2.3162F, -13.1915F, 0.3946F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(-1.7538F, 9.8466F, -20.1511F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(1.2446F, 6.4932F, 0.1834F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-1.7538F, 9.8466F, -20.1511F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1F, KeyframeAnimations.degreeVec(1.2446F, 6.4932F, 0.1834F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(-2.3162F, -13.1915F, 0.3946F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(-1.7538F, 9.8466F, -20.1511F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();

        public static final AnimationDefinition eat = AnimationDefinition.Builder.withLength(0.6F).looping()
                .addAnimation("torso", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.15F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.degreeVec(0.0F, 8.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("antennae", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -12.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.15F, KeyframeAnimations.degreeVec(0.0F, 12.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.45F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, -12.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
                ))
                .build();
}