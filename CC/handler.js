const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const db = require('./dbhandler');
const nodemailer = require('nodemailer');
const { Storage } = require('@google-cloud/storage');
const path = require('path');
const storage = new Storage({
    projectId: "thrifttrove2",
});

// Fungsi autentikasi token
const authenticateToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];
    if (!token) return res.status(401).send('Access Denied: No Token Provided!');

    jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
        if (err) return res.status(403).send('Access Denied: Invalid Token!');
        req.user = user;
        next();
    });
};

// Fungsi untuk registrasi
const signUp = (req, res) => {
    const { username, password, email } = req.body;
    const hashedPassword = bcrypt.hashSync(password, 8);

    const sql = 'INSERT INTO users (username, password, email) VALUES (?, ?, ?)';
    db.query(sql, [username, hashedPassword, email], (err, result) => {
        if (err) return res.status(500).send('Error on the server.');

        const response = {
            auth: true,
            message: 'User registered successfully',
            user: {
                id: result.insertId,
                username: username,
                email: email
            }
        };

        res.status(201).send(response);
    });
};

// Fungsi untuk login
const login = (req, res) => {
    const { username, password } = req.body;
    const sql = 'SELECT * FROM users WHERE username = ?';

    db.query(sql, [username], (err, results) => {
        if (err) return res.status(500).send('Error on the server.');
        if (results.length === 0) return res.status(404).send('No user found.');

        const user = results[0];
        const passwordIsValid = bcrypt.compareSync(password, user.password);

        if (!passwordIsValid) return res.status(401).send({ auth: false, token: null });

        const token = jwt.sign({ id: user.id }, process.env.JWT_SECRET, { expiresIn: 86400 });
        res.status(200).send({ auth: true, token });
    });
};

// Fungsi untuk mendapatkan profil pengguna
const getProfile = (req, res) => {
    const userId = req.user.id;
    const sql = 'SELECT id, username, email FROM users WHERE id = ?';

    db.query(sql, [userId], (err, results) => {
        if (err) {
            console.error('Error fetching profile:', err);
            return res.status(500).send('Error fetching profile.');
        }
        if (results.length === 0) return res.status(404).send('User not found.');

        const user = results[0];
        res.status(200).json(user);
    });
};

// Fungsi untuk mengambil semua item
const getItems = (req, res) => {
    const sql = 'SELECT * FROM items';
    db.query(sql, (err, results) => {
        if (err) return res.status(500).send('Error on the server.');
        res.status(200).send(results);
    });
};

// Fungsi untuk mengambil item berdasarkan pemilik
const getItemsByOwnerId = (req, res) => {
    const ownerId = req.user.id;
    const sql = 'SELECT * FROM items WHERE owner_id = ?';

    db.query(sql, [ownerId], (err, results) => {
        if (err) {
            console.error(err);
            return res.status(500).send('Error on the server.');
        }
        res.status(200).send(results);
    });
};

// Fungsi untuk mengambil item berdasarkan ID
const getItemById = (req, res) => {
    const { id } = req.params;
    const sql = 'SELECT * FROM items WHERE id = ?';
    db.query(sql, [id], (err, result) => {
        if (err) return res.status(500).send('Error on the server.');
        if (result.length === 0) return res.status(404).send('No item found.');
        res.status(200).send(result[0]);
    });
};


const bucketName = "assets_thrifttrove2";
const bucket = storage.bucket(bucketName);  

// Fungsi untuk menambahkan item
const addItem = (req, res) => {
    const { name, description, price } = req.body;
    const imageFile = req.file;

    if (!imageFile) {
        return res.status(400).send('No image file provided.');
    }

    const filename = Date.now() + path.extname(imageFile.originalname);
    const blob = bucket.file(filename);

    const blobStream = blob.createWriteStream({
        resumable: false,
        contentType: imageFile.mimetype,
    });

    blobStream.on('error', (err) => {
        console.error(err);
        return res.status(500).send('Error uploading image.');
    });

    blobStream.on('finish', () => {
        const imageUrl = `https://storage.googleapis.com/${bucketName}/${filename}`;

        const ownerId = req.user.id;

        // Ambil username dan email dari tabel users
        const getUserSql = 'SELECT username, email FROM users WHERE id = ?';
        db.query(getUserSql, [ownerId], (err, results) => {
            if (err) {
                console.error('Error fetching user data:', err);
                return res.status(500).send('Error on the server.');
            }

            if (results.length === 0) {
                return res.status(404).send('User not found.');
            }

            const username = results[0].username;
            const email = results[0].email;

            // Insert data ke dalam tabel items beserta username dan email
            const sql = 'INSERT INTO items (name, description, price, image, owner_id, username, email) VALUES (?, ?, ?, ?, ?, ?, ?)';
            db.query(sql, [name, description, price, imageUrl, ownerId, username, email], (err, result) => {
                if (err) {
                    console.error('Error inserting item:', err);
                    return res.status(500).send('Error on the server.');
                }
                res.status(201).send({ id: result.insertId });
            });
        });
    });

    blobStream.end(imageFile.buffer);
};


// Fungsi untuk memperbarui item
const updateItem = (req, res) => {
    const { id } = req.params;
    const { name, description, price } = req.body;
    const image = req.file ? req.file.filename : null;

    let sql = 'UPDATE items SET name = ?, description = ?, price = ?';
    const params = [name, description, price];

    if (image) {
        sql += ', image = ?';
        params.push(image);
    }

    sql += ' WHERE id = ?';
    params.push(id);

    db.query(sql, params, (err, result) => {
        if (err) return res.status(500).send('Error on the server.');
        if (result.affectedRows === 0) return res.status(404).send('No item found to update.');

        res.status(200).send({ message: 'Item updated successfully' });
    });
};

// Fungsi untuk menghapus item
const deleteItem = (req, res) => {
    const { id } = req.params;

    const sql = 'DELETE FROM items WHERE id = ?';
    db.query(sql, [id], (err, result) => {
        if (err) return res.status(500).send('Error on the server.');
        if (result.affectedRows === 0) return res.status(404).send('No item found to delete.');

        res.status(200).send({ message: 'Item deleted successfully' });
    });
};

// Fungsi untuk menambahkan item ke keranjang
const addItemToCart = (req, res) => {
    const { itemId, quantity } = req.body;
    const userId = req.user.id;

    // Ambil informasi nama, price, dan image item dari tabel items
    const getItemSql = 'SELECT name, price, image FROM items WHERE id = ?';
    db.query(getItemSql, [itemId], (err, results) => {
        if (err) return res.status(500).send('Error on the server.');
        if (results.length === 0) return res.status(404).send('Item not found.');

        const itemName = results[0].name;
        const itemPrice = results[0].price;
        const itemImage = results[0].image;

        // Cek apakah item sudah ada di keranjang
        const checkSql = 'SELECT * FROM cart WHERE user_id = ? AND item_id = ?';
        db.query(checkSql, [userId, itemId], (err, results) => {
            if (err) return res.status(500).send('Error on the server.');

            if (results.length > 0) {
                // Jika item sudah ada di keranjang, tambahkan jumlahnya
                const updateSql = 'UPDATE cart SET quantity = quantity + ?, item_name = ?, price = ?, image = ? WHERE user_id = ? AND item_id = ?';
                db.query(updateSql, [quantity, itemName, itemPrice, itemImage, userId, itemId], (err, result) => {
                    if (err) return res.status(500).send('Error on the server.');
                    return res.status(200).send({ message: 'Cart updated successfully' });
                });
            } else {
                // Jika item belum ada di keranjang, tambahkan item baru
                const sql = 'INSERT INTO cart (user_id, item_id, item_name, price, image, quantity) VALUES (?, ?, ?, ?, ?, ?)';
                db.query(sql, [userId, itemId, itemName, itemPrice, itemImage, quantity], (err, result) => {
                    if (err) return res.status(500).send('Error on the server.');
                    return res.status(201).send({ message: 'Item added to cart successfully', cart_id: result.insertId });
                });
            }
        });
    });
};

// Fungsi untuk mengupdate item di keranjang
const updateCartItem = (req, res) => {
    const { cartId, quantity } = req.body;

    const sql = 'UPDATE cart SET quantity = ? WHERE id = ?';
    db.query(sql, [quantity, cartId], (err, result) => {
        if (err) {
            console.error(err);
            return res.status(500).send('Error on the server.');
        }
        if (result.affectedRows === 0) return res.status(404).send('No cart item found to update.');
        res.status(200).send({ message: 'Cart item updated successfully' });
    });
};

// Fungsi untuk menghapus item dari keranjang
const deleteCartItem = (req, res) => {
    const { id } = req.params; // Ambil 'id' dari parameter URL

    const sql = 'DELETE FROM cart WHERE id = ?';
    db.query(sql, [id], (err, result) => {
        if (err) {
            console.error(err);
            return res.status(500).send('Error on the server.');
        }
        if (result.affectedRows === 0) return res.status(404).send('No cart item found to delete.');
        res.status(200).send({ message: 'Cart item deleted successfully' });
    });
};

// Fungsi checkout
const checkout = (req, res) => {
    const userId = req.user.id;
    const { address, name } = req.body;

    const getCartSql = 'SELECT * FROM cart WHERE user_id = ?';
    db.query(getCartSql, [userId], (err, cartItems) => {
        if (err) {
            return res.status(500).send('Error on the server.');
        }
        if (cartItems.length === 0) {
            return res.status(400).send('No items in cart.');
        }

        const createOrderItemsSql = 'INSERT INTO order_items (item_id, quantity, address, name) VALUES ?';
        const orderItems = cartItems.map(item => [item.item_id, item.quantity, address, name]);

        db.query(createOrderItemsSql, [orderItems], (err, result) => {
            if (err) {
                return res.status(500).send('Error on the server.');
            }

            const clearCartSql = 'DELETE FROM cart WHERE user_id = ?';
            db.query(clearCartSql, [userId], (err) => {
                if (err) {
                    return res.status(500).send('Error on the server.');
                }
                res.status(200).send({ message: 'Checkout successful' });
            });
        });
    });
};


// Fungsi untuk mendapatkan keranjang
const getCart = (req, res) => {
    const userId = req.user.id;
    const sql = 'SELECT * FROM cart WHERE user_id = ?';

    db.query(sql, [userId], (err, results) => {
        if (err) {
            console.error('Error fetching cart:', err);
            return res.status(500).send('Error fetching cart.');
        }

        // Ambil nama item dari tabel items untuk setiap item di keranjang
        const cartItems = results.map(item => {
            return {
                id: item.id,
                user_id: item.user_id,
                item_id: item.item_id,
                item_name: item.item_name,
                price: item.price,
                image: item.image,
                quantity: item.quantity
            };
        });

        res.status(200).json(cartItems);
    });
};


module.exports = {
    authenticateToken,
    signUp,
    login,
    getProfile,
    getItems,
    getItemById,
    addItem,
    updateItem,
    deleteItem,
    addItemToCart,
    updateCartItem,
    deleteCartItem,
    checkout,
    getItemsByOwnerId,
    getCart
};
