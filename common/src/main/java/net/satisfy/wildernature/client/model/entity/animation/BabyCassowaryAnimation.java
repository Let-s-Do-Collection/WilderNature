package net.satisfy.wildernature.client.model.entity.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class BabyCassowaryAnimation {
        public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength(1.1F).looping()
                .addAnimation("cassowary", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("cassowary", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -0.25F, -0.25F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.65F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .build();

        public static final AnimationDefinition walk = AnimationDefinition.Builder.withLength(0.7F).looping()
                .addAnimation("cassowary", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, 12.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.15F, KeyframeAnimations.degreeVec(2.5F, 12.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.degreeVec(-5.0F, -12.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(2.5F, -12.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(-5.0F, 12.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("cassowary", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.15F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.75F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, 0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.75F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(4.8813F, -10.2096F, -0.4634F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.15F, KeyframeAnimations.degreeVec(14.8813F, -10.2096F, -0.4634F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.degreeVec(-39.6765F, 13.5288F, 3.1329F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-2.6261F, 9.7194F, 1.2286F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(4.8813F, -10.2096F, -0.4634F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -0.275F, -0.7F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.2F, KeyframeAnimations.posVec(0.0F, 0.295F, 0.35F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, 0.5F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, -0.175F, -1.75F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, -0.275F, -0.7F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.6406F, -14.4452F, -2.5204F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.2F, KeyframeAnimations.degreeVec(5.6406F, -14.4452F, -2.5204F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.6406F, 14.4452F, 2.5204F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.degreeVec(5.6406F, 14.4452F, 2.5204F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.6406F, -14.4452F, -2.5204F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.2F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-39.6765F, -13.5288F, -3.1329F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.15F, KeyframeAnimations.degreeVec(-2.6261F, -9.7194F, -1.2286F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.degreeVec(4.8813F, 10.2096F, 0.4634F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(14.8813F, 10.2096F, 0.4634F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(-39.6765F, -13.5288F, -3.1329F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.5F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.2F, KeyframeAnimations.posVec(0.0F, -0.175F, -1.75F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, -0.275F, -0.7F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, 0.295F, 0.35F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 0.5F, -1.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .build();

        public static final AnimationDefinition play_growl = AnimationDefinition.Builder.withLength(2.3F)
                .addAnimation("cassowary", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.degreeVec(-0.019F, 4.9809F, -5.4369F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(-0.019F, -4.9809F, 5.4369F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(-0.019F, 4.9809F, -5.4369F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.45F, KeyframeAnimations.degreeVec(-0.019F, -4.9809F, 5.4369F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.8F, KeyframeAnimations.degreeVec(-0.019F, 4.9809F, -5.4369F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("cassowary", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.posVec(-0.5F, -0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.posVec(0.5F, -0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(-0.5F, -0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.45F, KeyframeAnimations.posVec(0.5F, -0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.8F, KeyframeAnimations.posVec(-0.5F, -0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0767F, 9.9616F, 5.8804F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(-0.6965F, 5.4138F, -5.2182F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.0767F, 9.9616F, 5.8804F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.45F, KeyframeAnimations.degreeVec(-0.6965F, 5.4138F, -5.2182F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.0767F, 9.9616F, 5.8804F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.posVec(0.2F, 0.4F, -0.175F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.posVec(-0.05F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.2F, 0.4F, -0.175F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.45F, KeyframeAnimations.posVec(-0.05F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.8F, KeyframeAnimations.posVec(0.2F, 0.4F, -0.175F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.degreeVec(17.5625F, 34.1727F, -10.4472F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(10.0625F, -34.1727F, 10.4472F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.15F, KeyframeAnimations.degreeVec(-4.9375F, 34.1727F, -10.4472F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.5F, KeyframeAnimations.degreeVec(7.5625F, -34.1727F, 10.4472F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.85F, KeyframeAnimations.degreeVec(2.5625F, 34.1727F, -10.4472F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.15F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.5F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.85F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.degreeVec(-0.6965F, -5.4138F, 5.2182F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0767F, -9.9616F, -5.8804F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(-0.6965F, -5.4138F, 5.2182F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.45F, KeyframeAnimations.degreeVec(0.0767F, -9.9616F, -5.8804F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.8F, KeyframeAnimations.degreeVec(-0.6965F, -5.4138F, 5.2182F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.posVec(0.05F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.posVec(-0.2F, 0.4F, -0.175F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.05F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.45F, KeyframeAnimations.posVec(-0.2F, 0.4F, -0.175F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.8F, KeyframeAnimations.posVec(0.05F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .build();
}