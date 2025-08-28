import React, { useState, useEffect } from "react";
import PortsSidebar from "./components/PortsSidebar";
import { BottomBar } from "./components/bar/BottomBar";
import { TopBar } from "./components/bar/TopBar";
import { useAISummary, useDataLoader } from "./hooks/loader";
import { GlobeEarth } from "./components/globe/Earth";
import styled from "styled-components";
import AISummaryChat from "./components/AISummaryChat";

const AppContainer = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: radial-gradient(ellipse at center, #10131a 0%, #05070d 100%);
`;
const AppBody = styled.div`
  width: 100%;
  height: calc(100% - 80px);
  display: flex;
  flex-direction: column;
  position: relative;
`;

const App: React.FC = () => {
  const [isPortSidebarCollapsed, setIsPortSidebarCollapsed] =
    useState<boolean>(true);
  const [showHelp, setShowHelp] = useState<boolean>(false);
  const [isAISummaryExpanded, setIsAISummaryExpanded] =
    useState<boolean>(false);

  const loadData = useDataLoader();

  useEffect(() => {
    loadData();
  }, []);

  const handlePortSidebarToggle = () => {
    setIsPortSidebarCollapsed(!isPortSidebarCollapsed);
  };

  const handleHelpToggle = () => {
    setShowHelp(!showHelp);
  };

  const handleAISummaryToggle = () => {
    setIsAISummaryExpanded(!isAISummaryExpanded);
  };

  useAISummary();

  return (
    <AppContainer>
      <TopBar
        onMenuClick={handlePortSidebarToggle}
        onHelpToggle={handleHelpToggle}
        onAISummaryToggle={handleAISummaryToggle}
        isAISummaryExpanded={isAISummaryExpanded}
      />
      <AppBody>
        <PortsSidebar
          isCollapsed={isPortSidebarCollapsed}
          onToggleCollapse={handlePortSidebarToggle}
        />
        <GlobeEarth
          isPortSidebarCollapsed={isPortSidebarCollapsed}
          showHelp={showHelp}
          onHelpToggle={handleHelpToggle}
          onAISummaryToggle={handleAISummaryToggle}
        />
        <AISummaryChat isExpanded={isAISummaryExpanded} />
      </AppBody>
      <BottomBar />
    </AppContainer>
  );
};

export default App;
