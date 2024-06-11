# Menggunakan node versi terbaru sebagai base image
FROM node:latest

# Membuat direktori kerja di dalam container
WORKDIR /usr/src/app/CC

# Menyalin package.json dan package-lock.json untuk instalasi dependensi dari folder CC di host ke folder yang sama di container
COPY CC/package*.json ./

# Menginstal dependensi npm
RUN npm install

# Menyalin seluruh kode aplikasi dari folder CC di host ke folder yang sama di container
COPY CC/. .

# Menjalankan aplikasi
CMD ["node", "server.js"]
