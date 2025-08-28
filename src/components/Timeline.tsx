import styled from "styled-components";
import { useAppDispatch, useAppSelector } from "../redux/hook";
import { setCurrentYear } from "../redux/store";
import { IconButton } from "./Icon";

const TimelineContainer = styled.div`
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.3s ease;

  &.demo-highlight {
    background: linear-gradient(
      90deg,
      rgba(77, 208, 225, 0.3) 0%,
      rgba(0, 255, 136, 0.3) 50%,
      rgba(77, 208, 225, 0.3) 100%
    );
    border-radius: 8px;
    padding: 4px;
    box-shadow: 0 0 20px rgba(77, 208, 225, 0.5);
    transform: scale(1.02);
    animation: pulse 1s ease-in-out infinite alternate;
  }

  @keyframes pulse {
    from {
      box-shadow: 0 0 20px rgba(77, 208, 225, 0.5);
      transform: scale(1.02);
    }
    to {
      box-shadow: 0 0 30px rgba(77, 208, 225, 0.8);
      transform: scale(1.05);
    }
  }
`;

const TimelineDot = styled.div<{ $isActive: boolean }>`
  flex: 1;
  min-width: 39px;
  min-height: 39px;
  padding-top: 5px;
  display: flex;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
  transition: all 0.3s;
  cursor: pointer;
  flex-shrink: 0;
  position: relative;
  background: ${(ps) => (ps.$isActive ? "rgba(77, 208, 225, 0.6)" : "")};
  color: ${(ps) => (ps.$isActive ? "#fff" : "")};
  font-weight: bold;

  > small {
    margin-right: 3px;
    font-size: 60%;
    position: relative;
    bottom: -1px;
    font-weight: normal;
  }

  &:hover {
    color: #fff;
    background: rgba(77, 208, 225, 0.6);
    &::before {
      /* background-color: #fff; */
    }
  }

  &::before {
    content: "";
    position: absolute;
    top: 0px;
    left: 50%;
    width: 2px;
    margin-left: -1px;
    height: 10px;
    background-color: rgba(77, 208, 225, 0.2);
  }
`;

const Timeline = () => {
  const currentYear = useAppSelector((state) => state.year.currentYear);

  const years = useAppSelector((state) => {
    const ys = (state.loader.data?.earthData || [])
      .map((item: any) => {
        const d = new Date(item.date);
        return Number.isNaN(d.getTime()) ? null : d.getFullYear();
      })
      .filter((y: number | null) => y !== null) as number[];
    // unique and sorted
    return Array.from(new Set(ys)).sort((a, b) => a - b);
  });

  const currentIndex = years.indexOf(currentYear as number);
  const dispatch = useAppDispatch();

  const handleDotClick = (year: number) => {
    dispatch(setCurrentYear(year));
  };

  const handlePrevious = () => {
    if (currentIndex > 0) {
      dispatch(setCurrentYear(years[currentIndex - 1]));
    }
  };

  const handleNext = () => {
    if (currentIndex < years.length - 1) {
      dispatch(setCurrentYear(years[currentIndex + 1]));
    }
  };

  return (
    <TimelineContainer className="timeline-container">
      <IconButton
        onClick={handlePrevious}
        disabled={currentIndex <= 0}
        title="Previous Week"
        icon="left"
      />
      {years.map((year) => (
        <TimelineDot
          key={year}
          $isActive={year === currentYear}
          onClick={() => handleDotClick(year)}
        >
          {year}
        </TimelineDot>
      ))}
      <IconButton
        onClick={handleNext}
        disabled={currentIndex >= years.length - 1}
        title="Next Year"
        icon="right"
      />
    </TimelineContainer>
  );
};

export default Timeline;
