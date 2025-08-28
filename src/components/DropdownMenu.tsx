import React from "react";
import styled from "styled-components";

interface DropdownMenuProps {
  isVisible: boolean;
  onFilterClick: () => void;
}

const MenuContainer = styled.div<{ $visible: boolean }>`
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 4px;
  background: rgba(15, 25, 35, 0.95);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(77, 208, 225, 0.3);
  border-radius: 8px;
  padding: 4px;
  transform: translateY(${(props) => (props.$visible ? "0" : "-10px")});
  opacity: ${(props) => (props.$visible ? 1 : 0)};
  visibility: ${(props) => (props.$visible ? "visible" : "hidden")};
  transition: all 0.2s ease;
  z-index: 1100;
`;

const MenuItem = styled.div<{ hasSubmenu?: boolean }>`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 12px;
  color: #4dd0e1;
  font-size: 14px;
  cursor: pointer;
  border-radius: 6px;
  white-space: nowrap;
  transition: all 0.2s ease;
  position: relative;

  &:hover {
    background: rgba(77, 208, 225, 0.1);
  }

  svg {
    width: 14px;
    height: 14px;
  }

  &::after {
    content: ${(props) => (props.hasSubmenu ? '"▶"' : '""')};
    font-size: 10px;
    color: #78909c;
    margin-left: auto;
  }
`;

export const DropdownMenu: React.FC<DropdownMenuProps> = ({
  isVisible,
  onFilterClick,
}) => {
  return (
    <MenuContainer $visible={isVisible}>
      <MenuItem onClick={onFilterClick}>
        <span>Filter</span>
      </MenuItem>
    </MenuContainer>
  );
};
