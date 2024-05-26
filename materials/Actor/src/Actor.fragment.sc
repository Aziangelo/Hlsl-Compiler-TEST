$input v_color0, v_fog, v_light, v_texcoord0

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/ActorUtil.dragonh>
#include <MinecraftRenderer.Materials/FogUtil.dragonh>

uniform vec4 SkyColor;
uniform vec4 ColorBased;
uniform vec4 ChangeColor;
uniform vec4 UseAlphaRewrite;
uniform vec4 TintedAlphaTestEnabled;
uniform vec4 MatColor;
uniform vec4 OverlayColor;
uniform vec4 TileLightColor;
uniform vec4 MultiplicativeTintColor;
uniform vec4 FogColor;
uniform vec4 FogControl;
uniform vec4 ActorFPEpsilon;
uniform vec4 LightDiffuseColorAndIlluminance;
uniform vec4 LightWorldSpaceDirection;
uniform vec4 FogAndDistanceControl;
uniform vec4 HudOpacity;
uniform vec4 UVAnimation;
uniform mat4 Bones[8];

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_MatTexture1);
#include <azify/utils/functions.glsl>

void main() {
#if DEPTH_ONLY
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    return;
#else

    vec4 albedo = getActorAlbedoNoColorChange(v_texcoord0, s_MatTexture, s_MatTexture1, MatColor);

#if ALPHA_TEST
    float alpha = mix(albedo.a, (albedo.a * OverlayColor.a), TintedAlphaTestEnabled.x);
    if(shouldDiscard(albedo.rgb, alpha, ActorFPEpsilon.x)) {
        discard;
    }
#endif // ALPHA_TEST

#if CHANGE_COLOR_MULTI
    albedo = applyMultiColorChange(albedo, ChangeColor.rgb, MultiplicativeTintColor.rgb);
#elif CHANGE_COLOR
    albedo = applyColorChange(albedo, ChangeColor, albedo.a);
    albedo.a *= ChangeColor.a;
#endif // CHANGE_COLOR_MULTI

#if ALPHA_TEST
    albedo.a = max(UseAlphaRewrite.r, albedo.a);
#endif

    //albedo = applyActorDiffuse(albedo, v_color0.rgb, vec4(1.0), ColorBased.x, OverlayColor);
    float isCaveX = smoothstep(0.65, 0.1, v_light.b);
    float isTorch = smoothstep(0.5, 1.0, v_light.r);
    isTorch =  (pow(isTorch, 6.)*0.5+isTorch*0.5);
    
    vec3 red = vec3(1.0,0.0, 0.0);
    vec3 gren = vec3(0.0, 1.0, 0.0);
    vec3 blue = vec3(0.0, 0.0, 1.0);
    mediump vec3 worldColor = timecycle3(vec3(0.9, 0.94, 1.0), vec3(0.34,0.26,0.22), vec3(0.43,0.43,0.67));
    worldColor = mix(worldColor, vec3(0.14,0.14,0.14), isCaveX);
    worldColor = mix(worldColor, vec3(1.0), isTorch);
    albedo.rgb *= worldColor;

#if TRANSPARENT
    albedo = applyHudOpacity(albedo, HudOpacity.x);
#endif

    albedo.rgb = applyFog(albedo.rgb, v_fog.rgb, v_fog.a);
    gl_FragColor = albedo;
#endif // DEPTH_ONLY
}
