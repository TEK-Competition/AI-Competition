import Globe from "globe.gl";

export const initGlobe = (el: HTMLElement) => {
  return new Globe(el, {})
    .width(el.clientWidth)
    .height(el.clientHeight)
    .pointOfView({ lat: 31.2304, lng: 121.4737, altitude: 2 })
    .showAtmosphere(true)
    .atmosphereAltitude(0.25);
};
