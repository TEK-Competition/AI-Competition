import type { GlobeInstance } from "globe.gl";

export const applyControls = (
  globe: GlobeInstance,
  globeContainer: HTMLElement
) => {
  const controls = globe.controls();
  controls.autoRotate = true;
  controls.autoRotateSpeed = 0.3;
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;

  // Pause rotation on mouse enter, resume on mouse leave
  globeContainer.addEventListener("mouseenter", () => {
    globe.controls().autoRotate = false;
  });
  globeContainer.addEventListener("mouseleave", () => {
    globe.controls().autoRotate = true;
  });
};
