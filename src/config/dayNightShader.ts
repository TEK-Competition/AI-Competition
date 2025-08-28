export const dayNightShader = {
  vertexShader: `
        // Passes world normal and UV to fragment shader
        varying vec3 vWorldNormal;
        varying vec2 vUv;
        void main() {
            vWorldNormal = normalize(mat3(modelMatrix) * normal); // Transform normal to world space
            vUv = uv; // Pass UV coordinates
            gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0); // Standard vertex transform
        }
    `,
  fragmentShader: `
        uniform sampler2D dayTexture;    // Daytime texture
        uniform sampler2D nightTexture;  // Nighttime texture
        uniform vec3 sunDirection;       // Direction of the sun

        varying vec3 vWorldNormal;       // World-space normal from vertex shader
        varying vec2 vUv;                // UV coordinates from vertex shader

        void main() {
            vec3 normNormal = normalize(vWorldNormal); // Normalize the normal
            float intensity = dot(normNormal, normalize(sunDirection)); // Sunlight intensity on the surface
            vec4 dayColor = texture2D(dayTexture, vUv);     // Sample day texture
            vec4 nightColor = texture2D(nightTexture, vUv); // Sample night texture

            // Only show night lights in the night area, not at all during the day
            float nightMask = 1.0 - smoothstep(-0.05, 0.10, intensity);

            // Enhance night lights (keep only the bright parts, make dark parts darker)
            vec3 nightLights = nightColor.rgb * vec3(2.5, 2.2, 2.0); // Boost night lights
            nightLights = clamp(nightLights, 0.0, 1.0); // Clamp to valid color range

            // Night area has a blue tint
            vec3 nightBase = mix(vec3(0.04, 0.09, 0.22), nightLights, 0.75);

            // Day area has a cool color tone
            vec3 coolDay = mix(dayColor.rgb, vec3(0.90, 0.97, 1.0), 0.10);

            // Blend between night and day based on mask
            vec3 finalColor = mix(nightBase, coolDay, 1.0 - nightMask);

            gl_FragColor = vec4(finalColor, 1.0); // Output final color
        }
    `,
};
