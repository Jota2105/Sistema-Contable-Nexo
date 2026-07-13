FROM node:22-alpine AS build
WORKDIR /app
COPY frontend_app/package*.json ./
RUN npm ci
COPY frontend_app/ ./
ARG VITE_GOOGLE_CLIENT_ID
ARG VITE_ENABLE_MOCK_LOGIN=false
ENV VITE_GOOGLE_CLIENT_ID=${VITE_GOOGLE_CLIENT_ID}
ENV VITE_ENABLE_MOCK_LOGIN=${VITE_ENABLE_MOCK_LOGIN}
ENV VITE_USE_MOCK_API=false
ENV VITE_API_URL=/api
RUN npm run build

FROM nginx:1.27-alpine
COPY deploy/local/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
