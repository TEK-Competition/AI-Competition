import type { GlobeInstance } from "globe.gl";
import { ShaderMaterial, TextureLoader, Vector2, Vector3 } from "three";
import earth_day from "../assets/2k_earth_day.jpg";
import earth_night from "../assets/2k_earth_night.jpg";
import night_sky from "../assets/night-sky.png";

const dayNightShader = {
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

function getSunPosition(): [number, number] {
  // Get current Beijing time
  const now = new Date();
  const beijingOffset = 8 * 60; // Beijing is UTC+8
  const utc = now.getTime() + now.getTimezoneOffset() * 60000;
  const beijing = new Date(utc + beijingOffset * 60000);

  // Sun longitude: At Beijing noon, the sun is at 120°E
  const hours = beijing.getHours() + beijing.getMinutes() / 60;
  const lng = 120 - (hours - 12) * 15;

  // Sun declination angle calculation
  const dayOfYear = Math.floor(
    (beijing.getTime() - new Date(beijing.getFullYear(), 0, 0).getTime()) /
      86400000
  );
  // Approximate declination angle formula
  const decl = -23.44 * Math.cos(((2 * Math.PI) / 365) * (dayOfYear + 10));
  return [lng, decl];
}

const calculateSunDirection = () => {
  const [lng, lat] = getSunPosition();
  // Convert latitude and longitude to spherical coordinates
  const phi = ((90 - lat) * Math.PI) / 180;
  const theta = ((90 - lng) * Math.PI) / 180;
  // Return direction vector
  return new Vector3(
    Math.sin(phi) * Math.cos(theta),
    Math.cos(phi),
    Math.sin(phi) * Math.sin(theta)
  );
};

export const setMaterial = (globe: GlobeInstance) => {
  const loader = new TextureLoader();
  globe.backgroundImageUrl(night_sky);

  const dayTexture = loader.load(earth_day);
  const nightTexture = loader.load(earth_night);
  const material = new ShaderMaterial({
    uniforms: {
      dayTexture: { value: dayTexture },
      nightTexture: { value: nightTexture },
      sunDirection: { value: calculateSunDirection() },
      globeRotation: { value: new Vector2() },
    },
    vertexShader: dayNightShader.vertexShader,
    fragmentShader: dayNightShader.fragmentShader,
    lights: false,
  });

  globe.globeMaterial(material);

  // Update sun direction every minute for day/night shader
  return setInterval(() => {
    material.uniforms.sunDirection.value.copy(calculateSunDirection());
  }, 120_000);
};

// Helper function to calculate great circle distance in radians
export function greatCircleDistance(
  start: { lat: number; lng: number },
  end: { lat: number; lng: number }
): number {
  const lat1 = (start.lat * Math.PI) / 180;
  const lat2 = (end.lat * Math.PI) / 180;
  const deltaLat = ((end.lat - start.lat) * Math.PI) / 180;
  const deltaLng = ((end.lng - start.lng) * Math.PI) / 180;

  const a =
    Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
    Math.cos(lat1) *
      Math.cos(lat2) *
      Math.sin(deltaLng / 2) *
      Math.sin(deltaLng / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return c; // Returns distance in radians
}
