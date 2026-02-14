// POS Logic
let products = [];
let categories = [];
let cart = [];
let selectedTable = null;
let selectedGroup = 'all';

// CSRF tokens
const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

document.addEventListener('DOMContentLoaded', async () => {
    await loadInitialData();
    setupEventListeners();
    renderAll();
});

async function loadInitialData() {
    try {
        // Load products
        const prodRes = await fetch('/api/backoffice/products');
        if (prodRes.ok) {
            const data = await prodRes.json();
            products = data.map(d => ({
                id: d.id,
                ...d.customFields
            }));
        }

        // Load categories (groups)
        const groupRes = await fetch('/api/backoffice/groups');
        if (groupRes.ok) {
            const data = await groupRes.json();
            categories = data.map(d => ({
                id: d.id,
                ...d.customFields
            }));
        }
    } catch (error) {
        console.error('Error loading data:', error);
    }
}

function setupEventListeners() {
    // Category filtering
    document.getElementById('categories-sidebar').addEventListener('click', (e) => {
        const item = e.target.closest('.category-item');
        if (!item) return;

        document.querySelectorAll('.category-item').forEach(el => el.classList.remove('active'));
        item.classList.add('active');
        selectedGroup = item.dataset.groupId;
        renderProducts();
    });

    // Search
    document.getElementById('productSearch').addEventListener('input', (e) => {
        renderProducts(e.target.value.toLowerCase());
    });

    // Table selection
    document.getElementById('btn-select-table').addEventListener('click', () => {
        const modal = new bootstrap.Modal(document.getElementById('tableModal'));
        modal.show();
    });

    document.getElementById('tables-list').addEventListener('click', (e) => {
        const btn = e.target.closest('.table-btn');
        if (!btn || btn.classList.contains('opacity-50')) return;

        selectedTable = {
            id: btn.dataset.id,
            name: btn.dataset.name
        };

        document.querySelectorAll('.table-btn').forEach(el => el.classList.remove('active'));
        btn.classList.add('active');
        document.getElementById('selected-table-name').textContent = `Table: ${selectedTable.name}`;

        bootstrap.Modal.getInstance(document.getElementById('tableModal')).hide();
    });

    // Payment method switch
    document.querySelectorAll('.payment-method-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.payment-method-btn').forEach(el => el.classList.remove('active'));
            btn.classList.add('active');

            const method = btn.dataset.method;
            document.getElementById('cash-payment-details').style.display = method === 'CASH' ? 'block' : 'none';
        });
    });

    // Quick cash buttons
    document.querySelectorAll('.money-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const val = parseFloat(btn.dataset.value);
            const input = document.getElementById('received-amount');
            const current = parseFloat(input.value || 0);
            input.value = (current + val).toFixed(2);
            calculateChange();
        });
    });

    document.getElementById('received-amount').addEventListener('input', calculateChange);

    // Proceed to payment
    document.getElementById('btn-proceed-payment').addEventListener('click', () => {
        const total = calculateTotal();
        document.getElementById('payment-total').textContent = formatPrice(total);
        document.getElementById('received-amount').value = total.toFixed(2);
        calculateChange();

        const modal = new bootstrap.Modal(document.getElementById('paymentModal'));
        modal.show();
    });

    // Complete order
    document.getElementById('btn-complete-order').addEventListener('click', submitOrder);
}

function calculateChange() {
    const total = calculateTotal();
    const received = parseFloat(document.getElementById('received-amount').value || 0);
    const change = Math.max(0, received - total);
    document.getElementById('payment-change').textContent = formatPrice(change);
}

function renderAll() {
    renderCategories();
    renderProducts();
    updateCartUI();
}

function renderCategories() {
    const sidebar = document.getElementById('categories-sidebar');
    let html = `
        <div class="category-item ${selectedGroup === 'all' ? 'active' : ''}" data-groupId="all">
            <i class="fas fa-th me-2"></i> All Products
        </div>
    `;

    categories.forEach(cat => {
        html += `
            <div class="category-item ${selectedGroup === cat.id ? 'active' : ''}" data-groupId="${cat.id}">
                <i class="${cat.icon || 'fas fa-tag'} me-2"></i> ${cat.name}
            </div>
        `;
    });

    sidebar.innerHTML = html;
}

function renderProducts(filter = '') {
    const grid = document.getElementById('products-grid');
    grid.innerHTML = '';

    const filtered = products.filter(p => {
        const matchesFilter = p.name.toLowerCase().includes(filter);
        const matchesGroup = selectedGroup === 'all' || (p.customizations && categories.find(c => c.id === selectedGroup)?.products.includes(p.id));
        // Note: The grouping logic depends on how groups/products are mapped. 
        // For now, I'll show all if filter is empty, or filter by name.
        return matchesFilter;
    });

    filtered.forEach(p => {
        if (!p.active) return;

        const card = document.createElement('div');
        card.className = 'col-md-3 col-6';
        card.innerHTML = `
            <div class="card product-card h-100" onclick="addToCart('${p.id}')">
                <img src="${p.image || '/images/placeholder.png'}" class="card-img-top" alt="${p.name}">
                <div class="card-body p-2">
                    <h6 class="card-title text-truncate mb-1">${p.name}</h6>
                    <div class="product-price">${formatPrice(p.price)}</div>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });
}

function addToCart(productId) {
    const product = products.find(p => p.id === productId);
    if (!product) return;

    // For simplicity, add directly. If it has customizations, we should show modal.
    // In this premium flow, we'll just add as a new item.

    const existing = cart.find(item => item.productId === productId && (!item.customizations || item.customizations.length === 0));

    if (existing) {
        existing.quantity++;
    } else {
        cart.push({
            id: btoa(Math.random()).substring(0, 8),
            productId: product.id,
            itemId: product.id, // DTO expectation
            name: product.name,
            price: product.price,
            quantity: 1,
            customizations: []
        });
    }

    updateCartUI();
}

function updateCartUI() {
    const container = document.getElementById('cart-items');
    if (cart.length === 0) {
        container.innerHTML = `
            <div class="text-center py-5 text-muted">
                <i class="fas fa-shopping-cart fa-3x mb-3 opacity-25"></i>
                <p>Order is empty</p>
            </div>
        `;
        document.getElementById('btn-proceed-payment').disabled = true;
    } else {
        let html = '';
        cart.forEach((item, index) => {
            html += `
                <div class="cart-item">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="fw-bold">${item.name}</span>
                        <span>${formatPrice(item.price * item.quantity)}</span>
                    </div>
                    <div class="d-flex justify-content-between align-items-center">
                        <div class="btn-group btn-group-sm">
                            <button class="btn btn-outline-secondary" onclick="updateQty(${index}, -1)">-</button>
                            <span class="px-3 border-top border-bottom d-flex align-items-center">${item.quantity}</span>
                            <button class="btn btn-outline-secondary" onclick="updateQty(${index}, 1)">+</button>
                        </div>
                        <button class="btn btn-link text-danger p-0" onclick="removeItem(${index})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            `;
        });
        container.innerHTML = html;
        document.getElementById('btn-proceed-payment').disabled = false;
    }

    const total = calculateTotal();
    document.getElementById('cart-subtotal').textContent = formatPrice(total);
    document.getElementById('cart-total').textContent = formatPrice(total);
}

function updateQty(index, delta) {
    cart[index].quantity += delta;
    if (cart[index].quantity <= 0) {
        cart.splice(index, 1);
    }
    updateCartUI();
}

function removeItem(index) {
    cart.splice(index, 1);
    updateCartUI();
}

function calculateTotal() {
    return cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
}

function formatPrice(val) {
    return new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(val);
}

async function submitOrder() {
    const method = document.querySelector('.payment-method-btn.active').dataset.method;
    const request = {
        items: cart,
        tableId: selectedTable ? selectedTable.id : null,
        paymentMethod: method,
        paid: true // In counter, usually paid immediately
    };

    const btn = document.getElementById('btn-complete-order');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Processing...';

    try {
        const res = await fetch('/counter/api/order', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(request)
        });

        if (res.ok) {
            const data = await res.json();
            alert(`Order #${data.orderNumber} created successfully!`);
            cart = [];
            selectedTable = null;
            document.getElementById('selected-table-name').textContent = 'Assign Table';
            updateCartUI();
            bootstrap.Modal.getInstance(document.getElementById('paymentModal')).hide();
        } else {
            alert('Error creating order');
        }
    } catch (error) {
        console.error('Error submitting order:', error);
        alert('Exception while submitting order');
    } finally {
        btn.disabled = false;
        btn.innerHTML = 'COMPLETE ORDER';
    }
}
