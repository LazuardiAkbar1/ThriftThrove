const express = require('express');
const multer = require('multer');
const handlers = require('./handler');

const router = express.Router();

const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/');
    },
    filename: (req, file, cb) => {
        cb(null, `${Date.now()}-${file.originalname}`);
    }
});

const upload = multer({ storage });
//tambah akun
router.post('/signup', handlers.signUp);
//masuk akun
router.post('/login', handlers.login);
//ambil semua item
router.get('/items', handlers.getItems);
// ambil item sesuai dengan id
router.get('/items/:id', handlers.getItemById);
//tambah item buat jualan
router.post('/items', upload.single('image'),handlers.authenticateToken, handlers.addItem);
//tambah cart
router.post('/cart', handlers.authenticateToken, handlers.addItemToCart);
//update product
router.put('/items/:id', upload.single('image'), handlers.authenticateToken, handlers.updateItem);
//delete product
router.delete('/items/:id', handlers.authenticateToken, handlers.deleteItem);
//ambil product seller
router.get('/itemsown', handlers.authenticateToken, handlers.getItemsByOwnerId);


module.exports = router;
