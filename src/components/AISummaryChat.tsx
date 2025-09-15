import React, { useEffect, useRef, useState } from "react";
import styled, { keyframes } from "styled-components";
import { useAppSelector } from "../redux/hook";
import ReactMarkdown from "react-markdown";

interface AISummaryChatProps {
    isExpanded: boolean;
}

const fadeIn = keyframes`
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
`;

const blink = keyframes`
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
`;

const Container = styled.div.withConfig({
    shouldForwardProp: (prop) => prop !== 'isExpanded',
})<{ isExpanded: boolean }>`
    position: absolute;
    top: 15px;
    left: 15px;
    max-width: ${props => props.isExpanded ? '30%' : '0'};
    max-height: ${props => props.isExpanded ? '70%' : '0'};
    width: ${props => props.isExpanded ? 'auto' : '0'};
    height: ${props => props.isExpanded ? 'auto' : '0'};
    background: linear-gradient(
        135deg,
        rgba(20, 25, 30, 0.95),
        rgba(30, 35, 40, 0.95)
    );
    backdrop-filter: blur(15px);
    border: 1px solid rgba(77, 208, 225, 0.3);
    border-radius: 12px;
    margin-left: 0; /* Remove margin since robot button is now in TopBar */
    color: #ebebeb;
    font-family: "Courier New", monospace;
    overflow: hidden; /* 隐藏溢出，防止内容显示在标题上方 */
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    z-index: 1000;
    opacity: ${props => props.isExpanded ? 1 : 0};
    animation: ${props => props.isExpanded ? fadeIn : 'none'} 0.3s ease-out;
    transition: all 0.3s ease-out;
    display: ${props => props.isExpanded ? 'flex' : 'none'};
    flex-direction: column;
`;

const Header = styled.div`
    position: sticky;
    top: 0;
    background: linear-gradient(
        135deg,
        rgba(20, 25, 30, 0.98),
        rgba(30, 35, 40, 0.98)
    );
    backdrop-filter: blur(15px);
    color: #4dd0e1;
    font-size: 14px;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.8px;
    text-shadow: 0 0 8px rgba(77, 208, 225, 0.4);
    padding: 8px 16px;
    border-bottom: 1px solid rgba(77, 208, 225, 0.2);
    border-radius: 12px 12px 0 0;
    z-index: 10;
    flex-shrink: 0; /* 防止标题被压缩 */
`;

const ContentWrapper = styled.div`
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden; /* 隐藏水平滚动条 */
    padding: 16px;

    &::-webkit-scrollbar {
        width: 6px;
    }

    &::-webkit-scrollbar-track {
        background: rgba(15, 25, 35, 0.3);
        border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
        background: rgba(77, 208, 225, 0.3);
        border-radius: 3px;

        &:hover {
            background: rgba(77, 208, 225, 0.5);
        }
    }
`;

const Content = styled.div<{ isVisible: boolean }>`
    font-size: 13px;
    line-height: 1.5;
    opacity: ${(props) => (props.isVisible ? 1 : 0)};
    transform: translateY(${(props) => (props.isVisible ? "0" : "10px")});
    transition: all 0.3s ease-out;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
    -webkit-font-smoothing: antialiased;
    text-rendering: optimizeLegibility;
    padding-top: 8px;

    p {
        margin: 0 0 12px 0;
        position: relative;
    }

    h1,
    h2,
    h3,
    h4,
    h5,
    h6 {
        color: #4dd0e1;
        margin: 16px 0 8px 0;
        font-size: 14px;
        font-weight: 600;
    }

    ul,
    ol {
        margin: 8px 0;
        padding-left: 20px;
    }

    li {
        margin: 4px 0;
    }

    code {
        background: rgba(77, 208, 225, 0.1);
        padding: 2px 4px;
        border-radius: 4px;
        font-family: "Courier New", monospace;
        font-size: 12px;
    }
`;

const Cursor = styled.span`
    display: inline-block;
    width: 2px;
    height: 1em;
    background-color: #4dd0e1;
    margin-left: 2px;
    vertical-align: middle;
    animation: ${blink} 0.8s step-start infinite;
`;

const AISummaryChat: React.FC<AISummaryChatProps> = ({ isExpanded }) => {
    const [isVisible, setIsVisible] = useState(false);
    const [displayedContent, setDisplayedContent] = useState("");
    const aiLoading = useAppSelector((state) => state.aiSummary?.loading);
    const aiError = useAppSelector((state) => state.aiSummary?.error);
    const aiLoaded = useAppSelector((state) => state.aiSummary?.loaded);
    const aiContent = useAppSelector((state) => state.aiSummary?.data?.content);
    const aiWeek = useAppSelector((state) => state.aiSummary?.data?.week);
    const currentYear = useAppSelector((state) => state.year.currentYear);
    const refTimer = useRef<any>();
    const containerRef = useRef<HTMLDivElement>(null);

    const scrollToBottom = () => {
        if (containerRef.current) {
            containerRef.current.scrollTop = containerRef.current.scrollHeight;
        }
    };

    useEffect(() => {
        return () => {
            clearInterval(refTimer.current);
        };
    }, []);

    useEffect(() => {
        const startTimer = (content: string) => {
            setDisplayedContent("");
            setIsVisible(true);
            let currentIndex = 0;
            const interval = setInterval(() => {
                if (currentIndex <= content.length) {
                    const str = content.slice(0, currentIndex);
                    setDisplayedContent(str);
                    currentIndex++;
                    // 每次更新内容后自动滚动到底部
                    setTimeout(scrollToBottom, 0);
                } else {
                    clearInterval(interval);
                }
            }, 15); // 调整这个值可以改变打字速度 (15ms = 2倍速度)
            refTimer.current = interval;
        };
        if (aiContent && aiWeek) {
            if (aiWeek !== currentYear) {
                clearInterval(refTimer.current);
                setIsVisible(false);
                setDisplayedContent("");
            } else {
                startTimer(aiContent);
            }
        }
    }, [aiContent, aiWeek, currentYear]);

    if (!aiLoaded && !aiLoading && !aiError) return null;

    return (
        <Container isExpanded={isExpanded}>
            <Header>
                {aiLoading
                    ? "AI总结中..."
                    : aiLoaded
                    ? `AI分析${currentYear}数据总结`
                    : "AI总结失败"}
            </Header>
            <ContentWrapper ref={containerRef}>
                {!aiLoading && aiLoaded && !aiError ? (
                    <Content isVisible={isVisible}>
                        <ReactMarkdown>{displayedContent}</ReactMarkdown>
                        {displayedContent !== aiContent && <Cursor />}
                    </Content>
                ) : (
                    <></>
                )}
            </ContentWrapper>
        </Container>
    );
};

export default AISummaryChat;
