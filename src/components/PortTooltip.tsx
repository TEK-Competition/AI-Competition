import React from 'react';
import styled from 'styled-components';
import type { LocationQuantityInfo } from '../type';

const TooltipContainer = styled.div<{ intensity: number; glowColor: string }>`
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
    padding: 12px 20px;
    border-radius: 14px;
    background: rgba(24, 28, 47, 0.98);
    box-shadow: 0 0 16px 4px #06a292cc, 0 0 32px 8px #ffe4e486;
    font-size: 1.1em;
    color: #f9f9f9;
    letter-spacing: 0.5px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.8);
    border: 2px solid #00ffe7;
    font-weight: 600;
    font-family: 'Arial', 'Helvetica', sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
`;

const InfoRow = styled.div`
    display: flex;
    align-items: center;
`;

const Label = styled.span<{ color: string }>`
    color: ${(props) => props.color};
`;

const Value = styled.span`
    font-weight: bold;
    margin-left: 4px;
`;

const PortTooltip: React.FC<LocationQuantityInfo> = (props: LocationQuantityInfo) => {
    const count = props.quantity ?? 0;
    const intensity = Math.min(1, Math.log10(Math.max(count, 1)) / Math.log10(1e9));

    return (
        <TooltipContainer intensity={intensity} glowColor={'#00ffe7'}>
            <InfoRow>
                <Label color="#00ffe7">Location: </Label>
                <Value>{props.location}</Value>
            </InfoRow>
            <InfoRow>
                <Label color="#ff8c00">Quantity: </Label>
                <Value>{count}</Value>
            </InfoRow>
        </TooltipContainer>
    );
};

export default PortTooltip;
