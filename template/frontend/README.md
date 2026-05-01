# Frontend

##  Creation

Created with
```
npm create vite@latest frontend
```

Running with

```
npm install
npm run dev
```

This is a minimal template with what I had created from the Spring Boot training..


## REACT
In React, the browser does the rendering. The server just sends one empty HTML file and a JavaScript bundle. React then runs in the browser and builds the page itself.
Browser → gets index.html + React JS bundle → React builds the page in the browser. This is why React is called a Single Page Application (SPA). There is only one HTML file (index.html) — React swaps content in and out without ever reloading the page.




This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
