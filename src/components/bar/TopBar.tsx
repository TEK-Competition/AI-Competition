import React from 'react';
import styled from 'styled-components';
import { BarContainer } from './Container';
import { IconButton } from '../Icon';
import { useAppDispatch, useAppSelector } from '../../redux/hook';
import { setViewMode, type ViewMode } from '../../redux/store';
import { useDataLoader } from '../../hooks/loader';
import { useState, useRef, useEffect } from 'react';

const FlexBox = styled.div`
    display: flex;
    flex-shrink: 0;
    flex: 1;
    align-items: center;
    justify-content: space-between;
`;

const MenuContainer = styled.div`
    position: relative;
    display: flex;
    align-items: center;
    gap: 8px;
`;

const ViewToggleContainer = styled.div`
    display: flex;
    gap: 4px;
    background: rgba(0, 0, 0, 0.15);
    backdrop-filter: blur(10px);
    border-radius: 6px;
    border: 1px solid rgba(255, 255, 255, 0.15);
    padding: 3px;
`;

const ViewToggleButton = styled.button<{ $isActive: boolean }>`
    padding: 6px 12px;
    border: none;
    border-radius: 4px;
    background: ${(props) => (props.$isActive ? 'rgba(131, 238, 252, 0.25)' : 'transparent')};
    color: ${(props) => (props.$isActive ? '#83eefc' : '#aaaaaa')};
    font-size: 12px;
    font-weight: ${(props) => (props.$isActive ? '700' : '500')};
    cursor: pointer;
    transition: all 0.25s ease;
    min-width: 48px;
    text-shadow: ${(props) => (props.$isActive ? '0 0 8px rgba(131, 238, 252, 0.6)' : 'none')};
    box-shadow: ${(props) => (props.$isActive ? '0 0 0 1px rgba(131, 238, 252, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.1)' : 'none')};

    &:hover {
        background: ${(props) => (props.$isActive ? 'rgba(131, 238, 252, 0.35)' : 'rgba(255, 255, 255, 0.08)')};
        color: ${(props) => (props.$isActive ? '#83eefc' : '#ffffff')};
        transform: translateY(-1px);
    }

    &:active {
        transform: translateY(0);
    }
`;

const HeaderTitle = styled.div`
    font-size: 14px;
    font-style: italic;
    color: #83eefc;
    letter-spacing: 1.5px;
    text-shadow: 1px 1px 0px #000, -1px -1px 0px #000, 1px -1px 0px #000, -1px 1px 0px #000,
        2px 2px 2px rgba(0, 0, 0, 0.8);
    filter: contrast(1.2) brightness(1.1);
    font-family: 'Arial Black', Arial, sans-serif;
`;

interface TopBarProps {
    onMenuClick: () => void;
    onHelpToggle?: () => void;
    onAISummaryToggle?: () => void;
    isAISummaryExpanded?: boolean;
}

const ViewToggle = () => {
    const dispatch = useAppDispatch();
    const currentViewMode = useAppSelector((state) => state.view.mode);
    const handleViewModeChange = (mode: ViewMode) => {
        dispatch(setViewMode(mode));
    };
    return (
        <ViewToggleContainer>
            {/* <ViewToggleButton
              $isActive={currentViewMode === 'all'}
              onClick={() => handleViewModeChange('all')}
            >
              全部
            </ViewToggleButton> */}
            <ViewToggleButton $isActive={currentViewMode === 'points'} onClick={() => handleViewModeChange('points')}>
                点图
            </ViewToggleButton>
            <ViewToggleButton $isActive={currentViewMode === 'arcs'} onClick={() => handleViewModeChange('arcs')}>
                连线
            </ViewToggleButton>
        </ViewToggleContainer>
    );
};

export const TopBar: React.FC<TopBarProps> = ({
    onMenuClick,
    onHelpToggle,
    onAISummaryToggle,
    isAISummaryExpanded,
}) => {
    const loader = useAppSelector((state) => state.loader);
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const dataLoader = useDataLoader();

    const handleRefresh = async () => {
        await dataLoader();
    };

    useEffect(() => {
        setIsLoading(loader.loading || false);
    }, [loader.loading]);

    return (
        <BarContainer position="top">
            <FlexBox>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <IconButton icon="filter" disabled={isLoading} onClick={onMenuClick} />
                    {onAISummaryToggle && <IconButton icon="robot" onClick={onAISummaryToggle} />}
                </div>
                <ViewToggle />
                <div>
                    <IconButton icon="refresh" disabled={isLoading} onClick={handleRefresh} />
                    {/* {onHelpToggle && <IconButton icon="help" onClick={onHelpToggle} />} */}
                </div>
            </FlexBox>
        </BarContainer>
    );
};
