package net.satisfy.wildernature.client.model.entity.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class LionAnimation {
        public static final AnimationDefinition pounce = AnimationDefinition.Builder.withLength(2.0F)
                .addAnimation("lion", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.7F, KeyframeAnimations.degreeVec(17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("lion", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, -1.25F, 2.75F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, -1.25F, 2.75F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 18.75F, -3.25F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.7F, KeyframeAnimations.posVec(0.0F, -0.5F, -6.25F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.95F, KeyframeAnimations.posVec(0.0F, 0.0F, -12.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.degreeVec(17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.degreeVec(12.3265F, 2.8631F, -6.9349F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.4F, KeyframeAnimations.degreeVec(12.3024F, -3.2085F, 1.0146F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.8F, KeyframeAnimations.degreeVec(-7.6976F, -3.2085F, 1.0146F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, 0.25F, -1.25F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.degreeVec(47.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.degreeVec(47.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(27.572F, 10.4354F, -4.0032F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.7F, KeyframeAnimations.degreeVec(-51.4167F, -30.5626F, 15.5068F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, 1.775F, -5.45F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, 1.775F, -5.45F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(-0.5F, -1.22F, 0.55F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.7F, KeyframeAnimations.posVec(-0.5F, -1.97F, -1.2F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.degreeVec(57.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.degreeVec(77.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.25F, KeyframeAnimations.degreeVec(-0.8334F, 1.9008F, 15.8472F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.6F, KeyframeAnimations.degreeVec(73.968F, -2.2365F, -10.6434F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.9066F, -12.6084F, 16.8075F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.9066F, 12.6084F, -16.8075F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.35F, KeyframeAnimations.degreeVec(-52.2862F, 7.4711F, 12.0994F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.7F, KeyframeAnimations.degreeVec(14.9059F, -8.7235F, -9.9139F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail_2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.degreeVec(47.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.degreeVec(47.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(27.572F, -10.4354F, 4.0032F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.7F, KeyframeAnimations.degreeVec(-51.4167F, 30.5626F, -15.5068F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, 1.775F, -5.45F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, 1.775F, -5.45F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.5F, -1.22F, 0.55F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.7F, KeyframeAnimations.posVec(0.5F, -1.97F, -1.2F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.degreeVec(32.6586F, 1.9135F, -3.2328F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.degreeVec(30.1586F, 1.9135F, -3.2328F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.degreeVec(-25.3066F, -32.5677F, -22.4296F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.75F, KeyframeAnimations.degreeVec(-36.851F, -6.0621F, -9.2065F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.posVec(0.25F, -3.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.posVec(0.75F, -3.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.posVec(0.0F, -3.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.75F, KeyframeAnimations.posVec(1.25F, 2.75F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.degreeVec(32.6586F, -1.9135F, 3.2328F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.degreeVec(30.1586F, -1.9135F, 3.2328F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.degreeVec(-25.3066F, 32.5677F, 22.4296F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.75F, KeyframeAnimations.degreeVec(-36.851F, 6.0621F, 9.2065F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.posVec(-0.25F, -3.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.posVec(-0.75F, -3.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.posVec(0.0F, -3.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.75F, KeyframeAnimations.posVec(-1.25F, 2.75F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .build();

        public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength(2.0F).looping()
                .addAnimation("lion", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("lion", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, -0.25F, -0.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.3608F, 8.8604F, -20.3453F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.3608F, -8.8604F, 20.3453F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.3608F, 8.8604F, -20.3453F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.247F, 19.5899F, -19.1523F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.15F, KeyframeAnimations.degreeVec(-12.247F, -19.5899F, 19.1523F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.247F, 19.5899F, -19.1523F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail_2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.15F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, -0.1F, 0.475F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, -0.1F, 0.475F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("upperBody", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("upperBody", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.25F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, -0.249F, -0.0218F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.2814F, 0.2254F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.2814F, 0.2254F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .build();

        public static final AnimationDefinition walk = AnimationDefinition.Builder.withLength(1.1F).looping()
                .addAnimation("lion", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-2.4929F, 0.3262F, 2.4786F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(4.9905F, -0.0008F, -0.019F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.degreeVec(-2.4929F, -0.3262F, -2.4786F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(4.9905F, 0.0008F, 0.019F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(-2.4929F, 0.3262F, 2.4786F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("lion", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.75F, 0.25F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -0.5F, -0.25F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, 0.75F, 0.25F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, -0.5F, -0.25F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.75F, 0.25F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-2.2054F, 16.7008F, -30.6569F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6F, KeyframeAnimations.degreeVec(-2.2054F, -16.7008F, 30.6569F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(-2.2054F, 16.7008F, -30.6569F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.247F, 19.5899F, -19.1523F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6F, KeyframeAnimations.degreeVec(-12.247F, -19.5899F, 19.1523F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(-12.247F, 19.5899F, -19.1523F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail_2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.2F, KeyframeAnimations.degreeVec(-5.4986F, 0.0916F, -0.7524F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.degreeVec(29.5213F, 3.0079F, 4.0174F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(16.8368F, 0.7742F, 0.5778F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(-0.0333F, 0.8429F, -1.8305F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3F, KeyframeAnimations.posVec(0.0068F, -0.3695F, -2.2488F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6F, KeyframeAnimations.posVec(-0.0081F, -0.7246F, 0.04F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.posVec(-0.0122F, 1.3555F, -1.3712F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(-0.0333F, 0.8429F, -1.8305F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(29.5213F, -3.0079F, -4.0174F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(16.8368F, -0.7742F, -0.5778F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(-5.4986F, -0.0916F, 0.7524F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(29.5213F, -3.0079F, -4.0174F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0081F, -0.7246F, 0.04F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3F, KeyframeAnimations.posVec(0.0122F, 1.3555F, -1.3712F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6F, KeyframeAnimations.posVec(0.0333F, 0.8429F, -1.8305F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.posVec(-0.0068F, -0.3695F, -2.2488F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0081F, -0.7246F, 0.04F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("upperBody", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(4.9907F, 0.434F, -2.4621F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(-4.981F, 0.2153F, 2.5332F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.degreeVec(4.9907F, -0.434F, 2.4621F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(-4.981F, -0.2153F, -2.5332F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(4.9907F, 0.434F, -2.4621F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("upperBody", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-7.4841F, -2.0553F, 5.1982F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3F, KeyframeAnimations.degreeVec(2.7841F, -2.0357F, -2.3066F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6F, KeyframeAnimations.degreeVec(-7.4841F, 2.0553F, -5.1982F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.85F, KeyframeAnimations.degreeVec(2.7841F, 2.0357F, 2.3066F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(-7.4841F, -2.0553F, 5.1982F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(9.4738F, -0.9398F, 0.1321F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.2F, KeyframeAnimations.degreeVec(17.0028F, -0.4205F, -2.3135F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-32.4753F, -1.3429F, -2.1089F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(-0.0477F, -1.0371F, 1.3785F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(9.4738F, -0.9398F, 0.1321F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(-0.0026F, -0.3124F, 1.1353F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.posVec(-0.0582F, 1.7046F, -1.6895F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.posVec(-0.019F, 0.5085F, -1.9503F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0188F, 0.2077F, -3.1421F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(-0.0026F, -0.3124F, 1.1353F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-32.4753F, 1.3429F, 2.1089F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.2F, KeyframeAnimations.degreeVec(-0.0477F, 1.0371F, -1.3785F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(9.4738F, 0.9398F, -0.1321F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(17.0028F, 0.4205F, 2.3135F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(-32.4753F, 1.3429F, 2.1089F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.019F, 0.5085F, -1.9503F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.posVec(-0.0188F, 0.2077F, -3.1421F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.posVec(0.0026F, -0.3124F, 1.1353F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0582F, 1.7046F, -1.6895F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.019F, 0.5085F, -1.9503F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .build();

        public static final AnimationDefinition bite = AnimationDefinition.Builder.withLength(0.9F)
                .addAnimation("lion", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("lion", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, 0.25F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.45F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6F, KeyframeAnimations.degreeVec(-47.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail_2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, 0.5F, 0.325F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -0.325F, 0.02F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("upperBody", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("upperBody", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.degreeVec(-9.8868F, -2.2567F, -7.184F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.degreeVec(15.1417F, 7.151F, 2.2671F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, -2.35F, -0.175F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 1.1F, -1.12F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, 0.5F, 0.325F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -0.325F, 0.02F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, -2.35F, -0.175F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 1.1F, -1.12F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .build();

        public static final AnimationDefinition fear = AnimationDefinition.Builder.withLength(1.65F)
                .addAnimation("lion", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.degreeVec(-14.9653F, 0.3094F, -0.0437F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(-5.5553F, -4.9509F, 0.6998F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.degreeVec(-5.5553F, 4.9509F, -0.6998F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(-5.5553F, -4.9509F, 0.6998F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.degreeVec(-5.5553F, 4.9509F, -0.6998F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("lion", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.posVec(-0.06F, 0.26F, 1.01F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -2.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.posVec(1.0F, -2.21F, -1.1F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.posVec(-1.0F, -2.21F, -1.1F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(1.0F, -2.21F, -1.1F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.posVec(-1.0F, -2.21F, -1.1F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.degreeVec(42.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(8.7431F, -14.6548F, 13.9793F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.95F, KeyframeAnimations.degreeVec(8.7431F, 14.6548F, -13.9793F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.15F, KeyframeAnimations.degreeVec(8.7431F, -14.6548F, 13.9793F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.35F, KeyframeAnimations.degreeVec(8.7431F, 14.6548F, -13.9793F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6F, KeyframeAnimations.degreeVec(-4.3066F, -18.5608F, 5.8619F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(-4.3066F, 18.5608F, -5.8619F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(-4.3066F, -18.5608F, 5.8619F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.2F, KeyframeAnimations.degreeVec(-4.3066F, 18.5608F, -5.8619F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.4F, KeyframeAnimations.degreeVec(-4.3066F, -18.5608F, 5.8619F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("tail_2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(10.9117F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.degreeVec(16.4623F, 0.6441F, -2.7021F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(13.1472F, 0.6522F, -2.7359F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.degreeVec(13.3607F, 1.2639F, -4.8368F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 1.175F, -1.025F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 2.675F, -1.025F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 2.67F, -0.89F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, 2.77F, -1.36F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 2.815F, -1.21F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.posVec(0.0F, 2.67F, -0.89F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("upperBody", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.degreeVec(-11.0797F, 0.2213F, -0.1361F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(8.415F, -3.0115F, 1.8521F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.degreeVec(8.415F, 3.0115F, -1.8521F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(8.415F, -3.0115F, 1.8521F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.degreeVec(8.415F, 3.0115F, -1.8521F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("upperBody", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.35F, KeyframeAnimations.degreeVec(14.7562F, 1.3498F, -0.0734F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.55F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8F, KeyframeAnimations.degreeVec(5.953F, -18.3643F, 0.9985F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(1.6125F, 28.2478F, 0.662F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.2F, KeyframeAnimations.degreeVec(-6.547F, -18.3643F, 0.9985F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.4F, KeyframeAnimations.degreeVec(5.953F, 18.3643F, -0.9985F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3F, KeyframeAnimations.degreeVec(-35.1792F, -37.2817F, -25.1669F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(3.8698F, -3.1012F, -24.9688F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(-4.7681F, -5.4388F, -19.9273F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.degreeVec(7.9313F, -11.6547F, -23.7343F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(-4.7681F, -5.4388F, -19.9273F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.degreeVec(7.9313F, -11.6547F, -23.7343F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3F, KeyframeAnimations.posVec(-0.2778F, -1.01F, 1.7582F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(1.275F, 1.15F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.posVec(0.3F, 2.85F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.posVec(2.25F, 1.8F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.3F, 2.85F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.posVec(2.25F, 1.8F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(13.3994F, -0.5797F, 2.4319F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.degreeVec(11.4623F, -0.6441F, 2.7021F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(15.6472F, -0.6522F, 2.7359F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.degreeVec(13.3994F, -0.5797F, 2.4319F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 1.175F, -1.025F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 2.675F, -1.025F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 2.67F, -0.89F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, 2.77F, -1.36F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 2.815F, -1.21F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.posVec(0.0F, 2.67F, -0.89F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3F, KeyframeAnimations.degreeVec(-35.1792F, 37.2817F, 25.1669F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(3.7355F, 3.049F, 22.4658F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.degreeVec(4.101F, 10.6442F, 27.3523F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.degreeVec(-3.4981F, -2.098F, 15.0728F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.degreeVec(4.101F, 10.6442F, 27.3523F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.degreeVec(-3.4981F, -2.098F, 15.0728F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3F, KeyframeAnimations.posVec(0.2778F, -1.01F, 1.7582F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(-1.275F, 1.15F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7F, KeyframeAnimations.posVec(-1.6F, 1.525F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9F, KeyframeAnimations.posVec(-0.925F, 3.525F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1F, KeyframeAnimations.posVec(-1.6F, 1.525F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3F, KeyframeAnimations.posVec(-0.925F, 3.525F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.65F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .build();

        public static final AnimationDefinition sleep = AnimationDefinition.Builder.withLength(2.0F).looping()
                .addAnimation("lion", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("lion", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -17.5F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(-0.035F, 0.1033F, -19.9977F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -17.5F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("tail_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(2.4722F, -0.1603F, -14.6681F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(21.7476F, 6.2796F, -13.6496F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(21.7623F, 6.2158F, -12.1494F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(21.7476F, 6.2796F, -13.6496F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, -42.5F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(-25.0448F, 0.0477F, -43.9986F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, -42.5F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(1.2886F, -1.4063F, 0.6014F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(1.2886F, -1.4063F, 0.6014F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("upperBody", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.6333F, -15.7118F, 10.0754F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(7.6107F, -14.1019F, 9.6502F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(5.6333F, -15.7118F, 10.0754F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("upperBody", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.1125F, -14.6382F, 29.0162F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(7.6853F, -14.981F, 30.4282F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(10.1125F, -14.6382F, 29.0162F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("h_head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(2.8546F, 0.8773F, -1.809F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(2.8546F, 0.8773F, -1.809F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-83.8164F, -4.4395F, 35.6306F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(-86.1938F, -4.5187F, 36.6078F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(-83.8164F, -4.4395F, 35.6306F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("leftArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.1102F, -6.5424F, -0.0186F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.1102F, -6.5424F, -0.0186F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0F, KeyframeAnimations.degreeVec(-81.3371F, 5.6861F, 35.4063F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(-83.7181F, 5.589F, 35.9638F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(-81.3371F, 5.6861F, 35.4063F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("rightArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0F, KeyframeAnimations.posVec(0.5038F, 1.9747F, -0.445F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(2.0F, KeyframeAnimations.posVec(0.5038F, 1.9747F, -0.445F), AnimationChannel.Interpolations.LINEAR)
                ))
                .build();
}