import type { ComponentProps, ButtonHTMLAttributes } from 'react';
import styled from 'styled-components';

export const Icon = ({
    type,
}: {
    type:
        | 'up'
        | 'down'
        | 'left'
        | 'right'
        | 'menu'
        | 'filter'
        | 'close'
        | 'refresh'
        | 'check'
        | 'ban'
        | 'check-circle'
        | 'check-circle-fill'
        | 'help'
        | 'robot';
}) => {
    if (type === 'check-circle-fill') {
        return (
            <svg fill="currentColor" width="16" height="16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                <path d="M8 15A7 7 0 1 0 8 1a7 7 0 0 0 0 14m-1.076-4.076a.6.6 0 0 1-.848 0l-2-2 .848-.848L6.5 9.65l4.576-4.575.848.848z"></path>
            </svg>
        );
    }
    if (type === 'check-circle') {
        return (
            <svg fill="currentColor" width="16" height="16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                <path d="m6.854 10.854 5-5-.707-.708L6.5 9.793 4.854 8.146l-.708.708 2 2a.5.5 0 0 0 .708 0"></path>
                <path d="M15 8A7 7 0 1 1 1 8a7 7 0 0 1 14 0m-1 0A6 6 0 1 0 2 8a6 6 0 0 0 12 0"></path>
            </svg>
        );
    }
    if (type === 'ban') {
        return (
            <svg fill="currentColor" width="16" height="16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                <path d="M15 8A7 7 0 1 1 1 8a7 7 0 0 1 14 0m-3.126 4.582L3.418 4.126a6 6 0 0 0 8.456 8.456m.707-.708a6 6 0 0 0-8.456-8.456z"></path>
            </svg>
        );
    }
    if (type === 'check') {
        return (
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path
                    fill-rule="evenodd"
                    d="M12.646 3.896a.5.5 0 0 1 .75.66l-.042.048-7.5 7.5a.5.5 0 0 1-.655.045l-.047-.04-2.5-2.429a.5.5 0 0 1 .649-.758l.047.04 2.147 2.085z"
                />
            </svg>
        );
    }
    if (type === 'down') {
        return (
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path
                    fill-rule="evenodd"
                    d="M3.146 5.396a.5.5 0 0 1 .66-.041l.048.041L8 9.543l4.146-4.147a.5.5 0 0 1 .66-.041l.048.041a.5.5 0 0 1 .041.66l-.041.048-4.5 4.5a.5.5 0 0 1-.66.041l-.048-.041-4.5-4.5a.5.5 0 0 1 0-.708"
                />
            </svg>
        );
    }
    if (type === 'left') {
        return (
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path
                    fill-rule="evenodd"
                    d="M5.396 7.646a.5.5 0 0 0-.041.66l.041.048 4.5 4.5a.5.5 0 0 0 .75-.66l-.042-.048L6.458 8l4.146-4.146a.5.5 0 0 0 .041-.66l-.041-.048a.5.5 0 0 0-.66-.041l-.048.041z"
                />
            </svg>
        );
    }

    if (type === 'right') {
        return (
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path
                    fill-rule="evenodd"
                    d="M10.604 7.646a.5.5 0 0 1 .041.66l-.041.048-4.5 4.5a.5.5 0 0 1-.75-.66l.042-.048L9.543 8 5.396 3.854a.5.5 0 0 1-.041-.66l.041-.048a.5.5 0 0 1 .66-.041l.048.041z"
                />
            </svg>
        );
    }

    if (type === 'up') {
        return (
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path
                    fill-rule="evenodd"
                    d="M7.646 5.396a.5.5 0 0 1 .66-.041l.048.041 4.5 4.5a.5.5 0 0 1-.66.75l-.048-.042L8 6.458l-4.146 4.146a.5.5 0 0 1-.66.041l-.048-.041a.5.5 0 0 1-.041-.66l.041-.048z"
                />
            </svg>
        );
    }
    if (type === 'menu') {
        return (
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path
                    fill-rule="evenodd"
                    d="M14.5 12.5a.5.5 0 0 1 .09.992l-.09.008h-13a.5.5 0 0 1-.09-.992l.09-.008zm0-5a.5.5 0 0 1 .09.992l-.09.008h-13a.5.5 0 0 1-.09-.992L1.5 7.5zm0-5a.5.5 0 0 1 .09.992l-.09.008h-13a.5.5 0 0 1-.09-.992L1.5 2.5z"
                />
            </svg>
        );
    }

    if (type === 'filter') {
        return (
            <svg width="24" height="24" viewBox="0 0 48 48" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                <path
                    d="M6 9L20.4 25.8178V38.4444L27.6 42V25.8178L42 9H6Z"
                    fill="currentColor"
                    stroke="#333"
                    stroke-width="4"
                    stroke-linejoin="round"
                />
            </svg>
        );
    }
    if (type === 'close') {
        return (
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path
                    fill-rule="evenodd"
                    d="M3.05 3.05a.5.5 0 0 1 .707 0L8 7.293l4.243-4.243a.5.5 0 0 1 .707.707L8.707 8l4.243 4.243a.5.5 0 0 1-.707.707L8 8.707 3.757 12.95a.5.5 0 0 1-.707-.707L7.293 8 3.05 3.757a.5.5 0 0 1 0-.707"
                />
            </svg>
        );
    }
    if (type === 'refresh') {
        return (
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path d="M2 8a6 6 0 0 1 9.969-4.5H10.5a.5.5 0 0 0 0 1h2.378c.345 0 .625-.28.625-.625V1.5a.5.5 0 1 0-1 0v1.14A7 7 0 1 0 15 8a.5.5 0 0 0-1 0A6 6 0 0 1 2 8" />
            </svg>
        );
    }
    if (type === 'help') {
        return (
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path d="M5.255 5.786a.237.237 0 0 0 .241.247h.825c.138 0 .248-.113.266-.25.09-.656.54-1.134 1.342-1.134.686 0 1.314.343 1.314 1.168 0 .635-.374.927-.965 1.371-.673.489-1.206 1.06-1.168 1.987l.003.217a.25.25 0 0 0 .25.246h.811a.25.25 0 0 0 .25-.25v-.105c0-.718.273-.927 1.01-1.486.609-.463 1.244-.977 1.244-2.056 0-1.511-1.276-2.241-2.673-2.241-1.267 0-2.655.59-2.75 2.286zm1.557 5.763c0 .533.425.927 1.01.927.609 0 1.028-.394 1.028-.927 0-.552-.42-.94-1.029-.94-.584 0-1.009.388-1.009.94z" />
            </svg>
        );
    }
    if (type === 'robot') {
        return (
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path d="M6 12.5a.5.5 0 0 1 .5-.5h4a.5.5 0 0 1 0 1h-4a.5.5 0 0 1-.5-.5M3 8.062C3 6.76 4.235 5.765 5.53 5.886a26.58 26.58 0 0 0 4.94 0C11.765 5.765 13 6.76 13 8.062v1.157a.933.933 0 0 1-.765.935c-.845.147-2.34.346-4.235.346-1.895 0-3.39-.2-4.235-.346A.933.933 0 0 1 3 9.219V8.062zm4.542-.827a.25.25 0 0 0-.217.068l-.92.9a24.767 24.767 0 0 1-1.871-.183.25.25 0 0 0-.068.495c.842.085 1.683.143 2.525.157a.25.25 0 0 0 .17-.312.25.25 0 0 0-.112-.137l.92-.9a.25.25 0 0 0-.427-.088zm-.542 1.14a.25.25 0 0 0-.217.068l-.92.9a24.767 24.767 0 0 1-1.871-.183.25.25 0 0 0-.068.495c.842.085 1.683.143 2.525.157a.25.25 0 0 0 .17-.312.25.25 0 0 0-.112-.137l.92-.9a.25.25 0 0 0-.427-.088z" />
                <path d="M8.5 1.866a1 1 0 1 0-1 0V3h-2A4.5 4.5 0 0 0 1 7.5V8a1 1 0 0 0-1 1v2a1 1 0 0 0 1 1v1a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-1a1 1 0 0 0 1-1V9a1 1 0 0 0-1-1v-.5A4.5 4.5 0 0 0 10.5 3h-2V1.866zM14 7.5V13a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1V7.5A3.5 3.5 0 0 1 5.5 4h5A3.5 3.5 0 0 1 14 7.5z" />
            </svg>
        );
    }
};

const ButtonContainer = styled.button`
    background: none;
    border: none;
    border-radius: 50%;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: inherit;
    cursor: ${(props) => (props.disabled ? 'not-allowed' : 'pointer')};
    color: ${(props) => (props.disabled ? '#666' : 'inherit')};

    > svg {
        color: inherit;
    }

    &:hover:not(:disabled) {
        background: rgba(77, 208, 225, 0.2);
        transform: scale(1.1);
    }

    &:active:not(:disabled) {
        transform: scale(0.95);
    }
`;

export const IconButton = (
    props: ButtonHTMLAttributes<HTMLButtonElement> & {
        icon: ComponentProps<typeof Icon>['type'];
    }
) => {
    return (
        <ButtonContainer type="button" {...props}>
            <Icon type={props.icon} />
        </ButtonContainer>
    );
};
