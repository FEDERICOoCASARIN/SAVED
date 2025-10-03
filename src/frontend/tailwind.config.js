// export const content = ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"];
// export const theme = {
// 	extend: {},
// };
// export const plugins = [];

import animate from "tailwindcss-animate";

export const content = ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"];
export const theme = {
  extend: {},
};
export const plugins = [require("tailwindcss-animate")];
