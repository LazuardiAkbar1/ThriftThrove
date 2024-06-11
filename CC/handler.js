const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const db = require('./dbhandler');
const nodemailer = require('nodemailer');

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

// Fungsi untuk menambahkan item
const addItem = (req, res) => {
    const { name, description, price } = req.body;
    const image = req.file.filename;
    const ownerId = req.user.id; 

    const sql = 'INSERT INTO items (name, description, price, image, owner_id) VALUES (?, ?, ?, ?, ?)';
    db.query(sql, [name, description, price, image, ownerId], (err, result) => {
        if (err) return res.status(500).send('Error on the server.');
        res.status(201).send({ id: result.insertId });
    });
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

    // Ambil informasi nama item dari tabel items
    const getItemSql = 'SELECT name FROM items WHERE id = ?';
    db.query(getItemSql, [itemId], (err, results) => {
        if (err) return res.status(500).send('Error on the server.');
        if (results.length === 0) return res.status(404).send('Item not found.');

        const itemName = results[0].name;

        // Cek apakah item sudah ada di keranjang
        const checkSql = 'SELECT * FROM cart WHERE user_id = ? AND item_id = ?';
        db.query(checkSql, [userId, itemId], (err, results) => {
            if (err) return res.status(500).send('Error on the server.');

            if (results.length > 0) {
                // Jika item sudah ada di keranjang, tambahkan jumlahnya
                const updateSql = 'UPDATE cart SET quantity = quantity + ?, item_name = ? WHERE user_id = ? AND item_id = ?';
                db.query(updateSql, [quantity, itemName, userId, itemId], (err, result) => {
                    if (err) return res.status(500).send('Error on the server.');
                    return res.status(200).send({ message: 'Cart updated successfully' });
                });
            } else {
                // Jika item belum ada di keranjang, tambahkan item baru
                const sql = 'INSERT INTO cart (user_id, item_id, item_name, quantity) VALUES (?, ?, ?, ?)';
                db.query(sql, [userId, itemId, itemName, quantity], (err, result) => {
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
    const { cartId } = req.body;

    const sql = 'DELETE FROM cart WHERE id = ?';
    db.query(sql, [cartId], (err, result) => {
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
    const { address, paymentMethod } = req.body;

    const getCartSql = 'SELECT * FROM cart WHERE user_id = ?';
    db.query(getCartSql, [userId], (err, cartItems) => {
        if (err) return res.status(500).send('Error on the server.');
        if (cartItems.length === 0) return res.status(400).send('No items in cart.');

        const createOrderSql = 'INSERT INTO orders (user_id, address, payment_method) VALUES (?, ?, ?)';
        db.query(createOrderSql, [userId, address, paymentMethod], (err, result) => {
            if (err) return res.status(500).send('Error on the server.');
            const orderId = result.insertId;

            const createOrderItemsSql = 'INSERT INTO order_items (order_id, item_id, quantity) VALUES ?';
            const orderItems = cartItems.map(item => [orderId, item.item_id, item.quantity]);

            db.query(createOrderItemsSql, [orderItems], (err) => {
                if (err) return res.status(500).send('Error on the server.');

                const clearCartSql = 'DELETE FROM cart WHERE user_id = ?';
                db.query(clearCartSql, [userId], (err) => {
                    if (err) return res.status(500).send('Error on the server.');
                    res.status(200).send({ message: 'Checkout successful', orderId });
                });
            });
        });
    });
};

// Fungsi untuk melacak pesanan
const trackOrder = (req, res) => {
    const { orderId } = req.params;

    const sql = 'SELECT * FROM orders WHERE id = ? AND user_id = ?';
    db.query(sql, [orderId, req.user.id], (err, results) => {
        if (err) return res.status(500).send('Error on the server.');
        if (results.length === 0) return res.status(404).send('No order found.');
        res.status(200).send(results[0]);
    });
};

// Fungsi untuk menangani pesan chat
const handleChatMessage = (socket, message) => {
    const userId = message.userId;
    const text = message.text;

    const sql = 'INSERT INTO chats (user_id, message) VALUES (?, ?)';
    db.query(sql, [userId, text], (err, result) => {
        if (err) return console.error('Error on the server.');
        socket.emit('receiveMessage', { userId, text });
    });
};

// Fungsi untuk melupakan password
const forgetPassword = (req, res) => {
    const { email } = req.body;

    const sql = 'SELECT * FROM users WHERE email = ?';
    db.query(sql, [email], (err, results) => {
        if (err) return res.status(500).send('Error on the server.');
        if (results.length === 0) return res.status(404).send('No user found with this email.');

        const user = results[0];
        const token = jwt.sign({ id: user.id }, process.env.JWT_SECRET, { expiresIn: 3600 });

        const transporter = nodemailer.createTransport({
            service: 'Gmail',
            auth: {
                user: process.env.EMAIL,
                pass: process.env.EMAIL_PASSWORD
            }
        });

        const mailOptions = {
            from: process.env.EMAIL,
            to: email,
            subject: 'Password Reset',
            text: `Click the link to reset your password: http://localhost:3000/reset-password?token=${token}`
        };

        transporter.sendMail(mailOptions, (error, info) => {
            if (error) {
                return res.status(500).send('Error on the server.');
            }
            res.status(200).send('Password reset link sent to your email.');
        });
    });
};

// Fungsi untuk mengatur ulang password
const resetPassword = (req, res) => {
    const { token, newPassword } = req.body;

    jwt.verify(token, process.env.JWT_SECRET, (err, decoded) => {
        if (err) return res.status(403).send('Access Denied: Invalid Token!');

        const hashedPassword = bcrypt.hashSync(newPassword, 8);
        const sql = 'UPDATE users SET password = ? WHERE id = ?';

        db.query(sql, [hashedPassword, decoded.id], (err, result) => {
            if (err) return res.status(500).send('Error on the server.');
            res.status(200).send('Password reset successfully.');
        });
    });
};

// Fungsi untuk mendapatkan keranjang
// Fungsi untuk mendapatkan keranjang
const getCart = (req, res) => {
    const userId = req.user.id;
    const sql = 'SELECT * FROM cart WHERE user_id = ?';

    db.query(sql, [userId], (err, results) => {
        if (err) return res.status(500).send('Error on the server.');

        // Ambil nama item dari tabel items untuk setiap item di keranjang
        const cartItems = results.map(item => {
            return {
                id: item.id,
                user_id: item.user_id,
                item_id: item.item_id,
                item_name: item.item_name,
                quantity: item.quantity
            };
        });

        res.status(200).send(cartItems);
    });
};


module.exports = {
    authenticateToken,
    signUp,
    login,
    getItems,
    getItemById,
    addItem,
    updateItem,
    deleteItem,
    addItemToCart,
    updateCartItem,
    deleteCartItem,
    checkout,
    trackOrder,
    handleChatMessage,
    forgetPassword,
    resetPassword,
    getItemsByOwnerId,
    getCart
};
