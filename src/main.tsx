import './style.css';
import { createRoot } from "react-dom/client";
import App from "./App";
import React from "react";
import { Provider } from "react-redux";
import { store } from "./redux/store";

const root = createRoot(document.getElementById("root")!);
root.render(
    <React.StrictMode>
        <Provider store={store}>
            <App />
        </Provider>
    </React.StrictMode>
);
