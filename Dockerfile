# Menggunakan node versi terbaru sebagai base image
FROM node:latest

# Membuat direktori kerja di dalam container
WORKDIR /usr/src/app

# Menyalin package.json dan package-lock.json untuk instalasi dependensi
COPY package*.json ./

# Menginstal dependensi npm
RUN npm install

# Menyalin seluruh kode aplikasi
COPY . .

# Menjalankan aplikasi
CMD ["node", "server.js"]
