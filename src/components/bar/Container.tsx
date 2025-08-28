import styled from "styled-components";

export const BarContainer = styled.div<{ position: "top" | "bottom" }>`
  width: 100%;
  height: 40px;
  flex-shrink: 0;
  background: rgba(15, 25, 35, 0.8);
  color: #4dd0e1;
  padding: 0 20px;
  font-size: 12px;
  border-bottom: ${(ps) => {
    return ps.position === "top" ? "1px solid rgba(77, 208, 225, 0.2)" : "none";
  }};
  border-top: ${(ps) => {
    return ps.position === "bottom"
      ? "1px solid rgba(77, 208, 225, 0.2)"
      : "none";
  }};
  font-family: "Courier New", monospace;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-sizing: border-box;
`;
