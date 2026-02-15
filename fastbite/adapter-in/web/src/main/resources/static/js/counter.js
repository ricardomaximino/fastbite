// POS Logic
let products = [];
let categories = [];
let allCustomizations = [];
let cart = [];
let selectedTable = null;
let selectedGroup = 'all';
let currentEditingItemIndex = null;
let editingOrderId = null;

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

        // Load customizations
        const custRes = await fetch('/api/backoffice/customizations');
        if (custRes.ok) {
            const data = await custRes.json();
            allCustomizations = data.map(d => ({
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

    document.getElementById('tables-list').addEventListener('click', async (e) => {
        const btn = e.target.closest('.table-btn');
        if (!btn || btn.classList.contains('opacity-50')) return;

        const tableId = btn.dataset.id;
        const tableName = btn.dataset.name;
        const status = btn.dataset.status;

        if (status === 'OCCUPIED' || status === 'BILLING') {
            document.getElementById('active-orders-table-name').textContent = tableName;
            selectedTable = { id: tableId, name: tableName };
            await loadTableOrders(tableId);
            bootstrap.Modal.getInstance(document.getElementById('tableModal')).hide();
            new bootstrap.Modal(document.getElementById('activeOrdersModal')).show();
        } else {
            selectAvailableTable(tableId, tableName);
            bootstrap.Modal.getInstance(document.getElementById('tableModal')).hide();
        }
    });

    // New order for table button
    document.getElementById('btn-new-order-table').addEventListener('click', () => {
        selectAvailableTable(selectedTable.id, selectedTable.name);
        bootstrap.Modal.getInstance(document.getElementById('activeOrdersModal')).hide();
    });

    // Save order button
    document.getElementById('btn-save-order').addEventListener('click', () => saveOrder());

    // Assign to table button
    document.getElementById('btn-assign-table').addEventListener('click', () => submitOrder(false));

    // Billing button
    document.getElementById('btn-billing').addEventListener('click', () => generateBilling());

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
    document.getElementById('btn-complete-order').addEventListener('click', () => submitOrder());

    // Customization modal submit
    document.getElementById('btn-add-with-customs').addEventListener('click', saveCustomizations);
}

function calculateChange() {
    const total = calculateTotal();
    const received = parseFloat(document.getElementById('received-amount').value || 0);
    const change = Math.max(0, received - total);
    document.getElementById('payment-change').textContent = formatPrice(change);
}

function selectAvailableTable(id, name) {
    selectedTable = { id, name };
    document.getElementById('selected-table-name').textContent = `Table: ${name}`;
    document.querySelectorAll('.table-btn').forEach(el => {
        el.classList.toggle('active', el.dataset.id === id);
    });
    updateCartUI(); // Refresh buttons state
}

async function loadTableOrders(tableId) {
    try {
        const res = await fetch(`/counter/api/tables/${tableId}/active-orders`);
        if (res.ok) {
            const orders = await res.json();
            const list = document.getElementById('active-orders-list');
            list.innerHTML = '';

            orders.forEach(order => {
                const item = document.createElement('button');
                item.className = 'list-group-item list-group-item-action d-flex justify-content-between align-items-center';
                item.innerHTML = `
                    <div>
                        <div class="fw-bold">Order #${order.orderNumber}</div>
                        <small class="text-muted">${new Date(order.createdAt).toLocaleTimeString()} - ${order.items.length} items</small>
                    </div>
                    <span class="badge bg-primary rounded-pill">${formatPrice(order.total)}</span>
                `;
                item.onclick = () => loadOrderIntoCart(order);
                list.appendChild(item);
            });
        }
    } catch (error) {
        console.error('Error loading table orders:', error);
    }
}

function loadOrderIntoCart(order) {
    editingOrderId = order.id;
    cart = order.items.map(item => ({
        id: item.id || btoa(Math.random()).substring(0, 8),
        productId: item.itemId,
        itemId: item.itemId,
        name: item.name,
        price: item.price,
        quantity: item.quantity,
        customizations: item.customizations || []
    }));

    document.getElementById('cart-actions-new').classList.add('d-none');
    document.getElementById('cart-actions-edit').classList.remove('d-none');
    updateCartUI();
    bootstrap.Modal.getInstance(document.getElementById('activeOrdersModal')).hide();
}

async function saveOrder() {
    if (!editingOrderId) return;

    const request = {
        items: cart,
        tableId: selectedTable ? selectedTable.id : null,
        paid: false // Saving updates doesn't mean payment yet
    };

    try {
        const res = await fetch(`/counter/api/orders/${editingOrderId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(request)
        });

        if (res.ok) {
            alert('Order updated successfully!');
            resetPOS();
        } else {
            alert('Error updating order');
        }
    } catch (error) {
        console.error('Error saving order:', error);
    }
}

async function generateBilling() {
    if (!selectedTable) return;
    try {
        const res = await fetch(`/counter/api/tables/${selectedTable.id}/status?status=BILLING`, {
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        });
        if (res.ok) {
            alert('Table set to BILLING status');
            resetPOS();
        }
    } catch (error) {
        console.error('Error updating table status:', error);
    }
}

function resetPOS() {
    cart = [];
    selectedTable = null;
    editingOrderId = null;
    document.getElementById('selected-table-name').textContent = 'Assign Table';
    document.getElementById('cart-actions-new').classList.remove('d-none');
    document.getElementById('cart-actions-edit').classList.add('d-none');
    updateCartUI();
    // Reload tables to reflect new statuses
    location.reload(); // Simplest way to refresh table statuses from model
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
            <div class="card product-card h-100" onclick="handleProductClick('${p.id}')">
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

function handleProductClick(productId) {
    addToCart(productId);
}

function addToCart(productId, quantity = 1, customizations = null) {
    const product = products.find(p => p.id === productId);
    if (!product) return;

    if (currentEditingItemIndex !== null) {
        cart[currentEditingItemIndex].quantity = quantity;
        cart[currentEditingItemIndex].customizations = customizations || [];
        currentEditingItemIndex = null;
    } else {
        const finalCustomizations = customizations || getDefaultCustomizations(product);
        cart.push({
            id: btoa(Math.random()).substring(0, 8),
            productId: product.id,
            itemId: product.id,
            name: product.name,
            price: product.price,
            quantity: quantity,
            customizations: finalCustomizations
        });
    }

    updateCartUI();
}

function getDefaultCustomizations(product) {
    const defaults = [];
    if (!product.customizations) return defaults;

    product.customizations.forEach(custId => {
        const customization = allCustomizations.find(c => c.id === custId);
        if (!customization) return;

        customization.options.forEach((opt, idx) => {
            if (opt.isSelectedByDefault) {
                defaults.push({
                    id: `${custId}-opt-${idx}`,
                    name: opt.name,
                    price: opt.price
                });
            }
        });
    });
    return defaults;
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
        document.getElementById('btn-assign-table').disabled = true;
    } else {
        let html = '';
        cart.forEach((item, index) => {
            const product = products.find(p => p.id === item.productId);
            const hasCustomizations = product && product.customizations && product.customizations.length > 0;

            html += `
                <div class="cart-item">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="fw-bold">${item.name}</span>
                        <span>${formatPrice(calculateItemPrice(item) * item.quantity)}</span>
                    </div>
                    ${item.customizations.length > 0 ? `<div class="small text-muted mb-2">${item.customizations.map(c => c.name).join(', ')}</div>` : ''}
                    <div class="d-flex justify-content-between align-items-center">
                        <div class="btn-group btn-group-sm">
                            <button class="btn btn-outline-secondary" onclick="updateQty(${index}, -1)">-</button>
                            <span class="px-3 border-top border-bottom d-flex align-items-center">${item.quantity}</span>
                            <button class="btn btn-outline-secondary" onclick="updateQty(${index}, 1)">+</button>
                        </div>
                        <div class="d-flex gap-2">
                             <button class="btn btn-link text-primary p-0 ${!hasCustomizations ? 'disabled opacity-25' : ''}" 
                                     onclick="${hasCustomizations ? `editItem(${index})` : ''}"
                                     ${!hasCustomizations ? 'disabled' : ''}>
                                <i class="fas fa-pencil-alt"></i>
                            </button>
                            <button class="btn btn-link text-danger p-0" onclick="removeItem(${index})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            `;
        });
        container.innerHTML = html;
        document.getElementById('btn-proceed-payment').disabled = false;
        document.getElementById('btn-assign-table').disabled = !selectedTable || cart.length === 0;
    }

    const total = calculateTotal();
    document.getElementById('cart-subtotal').textContent = formatPrice(total);
    document.getElementById('cart-total').textContent = formatPrice(total);
}

function calculateItemPrice(item) {
    let price = item.price;
    if (item.customizations) {
        item.customizations.forEach(c => {
            price += (c.price || 0);
        });
    }
    return price;
}

function editItem(index) {
    const item = cart[index];
    const product = products.find(p => p.id === item.productId);
    currentEditingItemIndex = index;
    showCustomizationModal(product, item.customizations);
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
    return cart.reduce((sum, item) => sum + (calculateItemPrice(item) * item.quantity), 0);
}

function formatPrice(val) {
    return new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(val);
}

async function submitOrder(paid = true) {
    const method = paid ? document.querySelector('.payment-method-btn.active').dataset.method : 'CASH';
    const request = {
        items: cart,
        tableId: selectedTable ? selectedTable.id : null,
        paymentMethod: method,
        paid: paid
    };

    const btn = paid ? document.getElementById('btn-complete-order') : document.getElementById('btn-assign-table');
    const originalHtml = btn.innerHTML;

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

            // If it's a new order with a table, set table to OCCUPIED via API
            if (request.tableId) {
                await fetch(`/counter/api/tables/${request.tableId}/status?status=OCCUPIED`, {
                    method: 'POST',
                    headers: { 'X-CSRF-TOKEN': csrfToken }
                });
            }

            alert(`Order #${data.orderNumber} created successfully!`);
            if (paid) {
                bootstrap.Modal.getInstance(document.getElementById('paymentModal')).hide();
            }
            resetPOS();
        } else {
            alert('Error creating order');
        }
    } catch (error) {
        console.error('Error submitting order:', error);
        alert('Exception while submitting order');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalHtml;
    }
}

function showCustomizationModal(product, currentCustomizations = []) {
    currentProduct = product;
    document.getElementById('product-modal-name').textContent = `Customize ${product.name}`;
    const optionsContainer = document.getElementById('customization-options');
    optionsContainer.innerHTML = '';

    if (!product.customizations) return;

    product.customizations.forEach(custId => {
        const customization = allCustomizations.find(c => c.id === custId);
        if (!customization) return;

        const section = document.createElement('div');
        section.className = 'mb-4';
        section.innerHTML = `<h6 class="fw-bold border-bottom pb-2"><i class="fas fa-utensils me-2"></i>${customization.name}</h6>`;

        customization.options.forEach((opt, idx) => {
            const isSelected = currentCustomizations.some(c => c.id === `${custId}-opt-${idx}`);
            const inputId = `${custId}-opt-${idx}`;
            const isRadio = customization.type === 'radio';

            const div = document.createElement('div');
            div.className = 'form-check mb-2';
            div.innerHTML = `
                <input class="form-check-input customization-input" 
                       type="${isRadio ? 'radio' : 'checkbox'}" 
                       name="${isRadio ? custId : inputId}" 
                       id="${inputId}" 
                       value="${opt.name}"
                       data-price="${opt.price}"
                       ${isSelected ? 'checked' : ''}>
                <label class="form-check-label d-flex justify-content-between w-100" for="${inputId}">
                    <span>${opt.name}</span>
                    <span class="text-muted">${opt.price > 0 ? '+' + formatPrice(opt.price) : ''}</span>
                </label>
            `;
            section.appendChild(div);
        });

        optionsContainer.appendChild(section);
    });

    const modal = new bootstrap.Modal(document.getElementById('customizationModal'));
    modal.show();
}

function saveCustomizations() {
    const customizations = [];
    const inputs = document.querySelectorAll('.customization-input:checked');

    inputs.forEach(input => {
        customizations.push({
            id: input.id,
            name: input.value,
            price: parseFloat(input.dataset.price || 0)
        });
    });

    const productId = currentProduct.id;
    addToCart(productId, 1, customizations);

    bootstrap.Modal.getInstance(document.getElementById('customizationModal')).hide();
}

let currentProduct = null;
