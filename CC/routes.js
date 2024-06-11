const { Storage } = require('@google-cloud/storage');
const storage = new Storage();
const bucket = storage.bucket('your-bucket-name'); // Ganti dengan nama bucket Anda

const uploadImageToGCS = (file) => {
    return new Promise((resolve, reject) => {
        const blob = bucket.file(Date.now() + '-' + file.originalname);
        const blobStream = blob.createWriteStream({
            resumable: false
        });

        blobStream.on('error', (err) => {
            reject(err);
        });

        blobStream.on('finish', () => {
            const publicUrl = `https://storage.googleapis.com/${bucket.name}/${blob.name}`;
            resolve(publicUrl);
        });

        blobStream.end(file.buffer);
    });
};


// Tambah akun
router.post('/signup', handlers.signUp);
// Masuk akun
router.post('/login', handlers.login);
// Ambil semua item
router.get('/items', handlers.getItems);
// Ambil item sesuai dengan id
router.get('/items/:id', handlers.getItemById);
// Tambah item buat jualan
router.post('/items', upload.single('image'), handlers.authenticateToken, handlers.addItem);
// Tambah cart
router.post('/cart', handlers.authenticateToken, handlers.addItemToCart);
// Update product
router.put('/items/:id', upload.single('image'), handlers.authenticateToken, handlers.updateItem);
// Delete product
router.delete('/items/:id', handlers.authenticateToken, handlers.deleteItem);
// Ambil product seller
router.get('/itemsown', handlers.authenticateToken, handlers.getItemsByOwnerId);

// Rute untuk checkout
router.post('/checkout', handlers.authenticateToken, handlers.checkout);

// Rute untuk CRUD cart
router.put('/cart', handlers.authenticateToken, handlers.updateCartItem);
router.get('/cart', handlers.authenticateToken, handlers.getCart);
router.delete('/cart', handlers.authenticateToken, handlers.deleteCartItem);


// Rute untuk tracking barang
router.get('/track/:orderId', handlers.authenticateToken, handlers.trackOrder);


module.exports = router;
