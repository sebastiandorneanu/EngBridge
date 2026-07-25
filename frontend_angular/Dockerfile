FROM node:20 AS build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

RUN ls -la /app/dist/frontend_angular/browser || echo "Directory not found!"

FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build /app/dist/frontend_angular/browser /usr/share/nginx/html

RUN ls -la /usr/share/nginx/html

RUN if [ ! -f /usr/share/nginx/html/index.html ] && [ -f /usr/share/nginx/html/index.csr.html ]; then \
        cp /usr/share/nginx/html/index.csr.html /usr/share/nginx/html/index.html; \
        echo "Renamed index.csr.html to index.html"; \
    fi

RUN chmod -R 755 /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
