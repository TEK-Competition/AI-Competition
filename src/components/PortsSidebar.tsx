import React from "react";
import styled from "styled-components";
import { useAppDispatch, useAppSelector } from "../redux/hook";
import { togglePort, selectAllPorts, clearSelectedPorts } from "../redux/store";
import { Icon, IconButton } from "./Icon";
import type { EarthDataItem, LocationInfo } from "../type";

interface PortsSidebarProps {
  isCollapsed: boolean;
  onToggleCollapse: () => void;
}

const SidebarContainer = styled.div`
  position: absolute;
  top: 15px;
  left: 15px;
  bottom: 15px;
  width: 15%;
  color: #4dd0e1;
  background: rgba(15, 25, 35, 0.6);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(77, 208, 225, 0.3);
  border-radius: 12px;
  z-index: 1005;
  font-family: "Courier New", monospace;
  overflow: hidden;
  display: flex;
  flex-direction: column;
`;

const MainContent = styled.div`
  padding: 10px;
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(15, 25, 35, 0.3);
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(77, 208, 225, 0.3);
    border-radius: 4px;

    &:hover {
      background: rgba(77, 208, 225, 0.5);
    }
  }
`;

const SectionHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 8px;
  background: rgba(77, 208, 225, 0.1);
  border-radius: 6px;
  font-weight: bold;
`;

const PortTag = styled.div<{ $selected: boolean }>`
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 30px;
  background: ${(props) =>
    props.$selected ? "rgba(77, 208, 225, 0.3)" : "rgba(15, 25, 35, 0.6)"};
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.2s ease;
  -webkit-font-smoothing: antialiased;
  text-rendering: optimizeLegibility;
  text-align: center;
  word-break: break-word;
  grid-column: auto / span 1;
  position: relative;

  /* 如果内容宽度超过200px，则占据整行 */
  &:has(> span[data-width="wide"]) {
    grid-column: 1 / -1;
  }

  &:hover {
    background: rgba(77, 208, 225, 0.2);
  }

  & > [data-selected] {
    position: absolute;
    right: 0;
    bottom: 0;
    display: flex;
    svg {
      width: 13px;
      height: 13px;
    }
  }
`;

const PortsGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
`;

const PortsBox = styled.div`
  margin-bottom: 10px;
  > h5 {
    margin-top: 0;
    margin-bottom: 10px;
  }
`;

const PortsSidebar: React.FC<PortsSidebarProps> = ({
  isCollapsed,
  onToggleCollapse,
}) => {
  const dispatch = useAppDispatch();
  const selectedLocations = useAppSelector((state) => state.ports.selectedLocations);
  const currentYear = useAppSelector((state) => state.year.currentYear);
  // 现在 loader.data?.earthData 应为 EarthDataItem[]
  const earthData = useAppSelector((state) => state.loader.data?.earthData as EarthDataItem[] | undefined);

  // 从 earthData (EarthDataItem[]) 动态聚合港口列表（包含 from/to），并按当年累计 quantity 降序排序
  interface PortSummary {
    city: string;
    latitude: number;
    longitude: number;
    totalQuantity: number;
  }

  const portsSummary = React.useMemo<PortSummary[]>(() => {
    if (!earthData || !currentYear) return [];

    // 仅保留与 currentYear 匹配的条目
    const yearItems = earthData.filter((it: EarthDataItem) => {
      if (!it?.date) return false;
      const d = new Date(it.date);
      if (Number.isNaN(d.getTime())) return false;
      return d.getFullYear() === Number(currentYear);
    });

    const map = new Map<string, PortSummary>();

    yearItems.forEach((item) => {
      const qty = typeof item.quantity === "number" ? item.quantity : 0;

      const add = (loc: LocationInfo | undefined) => {
        if (!loc || !loc.location) return;
        const name = loc.location;
        const existing = map.get(name);
        if (existing) {
          existing.totalQuantity += qty;
        } else {
          map.set(name, {
            city: name,
            latitude: loc.latitude ?? 0,
            longitude: loc.longitude ?? 0,
            totalQuantity: qty,
          });
        }
      };

      add(item.from);
      add(item.to);
    });

    return Array.from(map.values()).sort((a, b) => b.totalQuantity - a.totalQuantity);
  }, [earthData, currentYear]);

  const ports = React.useMemo(() => portsSummary.map((p) => p.city), [portsSummary]);

  // 获取当前选中港口的汇总数据（基于 totalQuantity）
  const selectedPortsData = React.useMemo(() => {
    if (!portsSummary || portsSummary.length === 0 || !selectedLocations) return [];
    return portsSummary.filter((p) => selectedLocations.includes(p.city));
  }, [portsSummary, selectedLocations]);

  const handlePortToggle = (portId: string) => {
    dispatch(togglePort(portId));
  };

  const handleSelectAll = () => {
    if (selectedLocations.length === ports.length) {
      dispatch(clearSelectedPorts());
    } else {
      dispatch(selectAllPorts(ports.concat([])));
    }
  };

  const handleClearAll = () => {
    dispatch(clearSelectedPorts());
  };

  return (
    <>
      {!isCollapsed && (
        <SidebarContainer>
          <SectionHeader>
            <span>Filter Ports</span>
            <div style={{ display: "flex" }}>
              {selectedLocations.length > 0 && (
                <IconButton icon="ban" onClick={handleClearAll} />
              )}
              <IconButton
                icon={
                  selectedLocations.length === ports.length
                    ? "check-circle-fill"
                    : "check-circle"
                }
                onClick={handleSelectAll}
              />
              <IconButton icon="close" onClick={onToggleCollapse} />
            </div>
          </SectionHeader>
          <MainContent>
            <PortsBox>
              <h5>All Locations ({currentYear})</h5>
              <PortsGrid>
                {ports.map((port: string) => {
                  const summary = portsSummary.find((p) => p.city === port);
                  const total = summary?.totalQuantity || 0;

                  return (
                    <PortTag
                      key={port}
                      $selected={selectedLocations.includes(port)}
                      onClick={() => handlePortToggle(port)}
                    >
                      <span data-width={port.length > 9 ? "wide" : "normal"}>
                        {port}
                        <br />
                        <small style={{ opacity: 0.8, fontSize: "10px" }}>
                          ({total})
                        </small>
                      </span>
                      {selectedLocations.includes(port) && (
                        <span data-selected="true">
                          <Icon type="check" />
                        </span>
                      )}
                    </PortTag>
                  );
                })}
              </PortsGrid>
            </PortsBox>
            
            {selectedPortsData.length > 0 && (
              <PortsBox>
                <h5>Selected Locations Summary</h5>
                <div style={{ 
                  padding: "10px", 
                  background: "rgba(77, 208, 225, 0.1)", 
                  borderRadius: "6px",
                  fontSize: "12px"
                }}>
                  <div>Total Selected: {selectedPortsData.length}</div>
                  <div>Total Quantity: {selectedPortsData.reduce((sum, port) => sum + (port.totalQuantity || 0), 0)}</div>
                  <div style={{ marginTop: "8px" }}>
                    {selectedPortsData
                      .sort((a, b) => b.totalQuantity - a.totalQuantity)
                      .map((port) => (
                        <div
                          key={port.city}
                          style={{
                            display: "flex",
                            justifyContent: "space-between",
                            marginBottom: "2px",
                          }}
                        >
                          <span>{port.city}</span>
                          <span>{port.totalQuantity}</span>
                        </div>
                      ))}
                  </div>
                </div>
              </PortsBox>
            )}
          </MainContent>
        </SidebarContainer>
      )}
    </>
  );
};

export default PortsSidebar;
