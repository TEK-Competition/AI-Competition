import React from "react";
import styled from "styled-components";

interface HeaderProps {
  title?: string;
}

const HeaderContainer = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  width: 100%;
  background: rgba(15, 25, 35, 0.8);
  backdrop-filter: blur(10px);
  color: #4dd0e1;
  padding: 8px 20px;
  font-size: 12px;
  font-weight: bold;
  text-align: center;
  text-transform: uppercase;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(77, 208, 225, 0.2);
  font-family: "Courier New", monospace;
  z-index: 1000;
`;

const Header: React.FC<HeaderProps> = ({
  title = "Dashboard",
}) => {
  return <HeaderContainer>▪ {title} ▪</HeaderContainer>;
};

export default Header;
