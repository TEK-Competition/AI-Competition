import ReactDOMServer from "react-dom/server";
import PortTooltip from "./PortTooltip";
import type { LocationQuantityInfo } from "../type";

export const renderPortTooltip = (props: LocationQuantityInfo): string => {
  return ReactDOMServer.renderToString(<PortTooltip {...props} />);
};
