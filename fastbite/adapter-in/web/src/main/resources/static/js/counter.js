// POS Logic
let products = [];
let categories = [];
let allCustomizations = [];
let cart = [];
let selectedTable = null;
let selectedGroup = 'all';
let currentEditingItemIndex = null;
let editingOrderId = null;
let allTables = [];
let isTableMode = false;
let tableOrders = [];
let reassigningOrderId = null;
let currentProduct = null;
let payingSingleOrderId = null;

// CSRF tokens
const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

// Translation dictionary helper
const i18nElement = document.getElementById('dictionary');
const t = (key) => i18nElement?.dataset[key] || key;

document.addEventListener('DOMContentLoaded', async () => {
    await loadInitialData();
    setupEventListeners();
    renderAll();
    renderTables();
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

        // Load tables
        const tableRes = await fetch('/api/backoffice/tables');
        if (tableRes.ok) {
            const data = await tableRes.json();
            allTables = data.map(d => ({
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

    // Table selection modal trigger
    document.getElementById('btn-select-table').addEventListener('click', () => {
        new bootstrap.Modal(document.getElementById('tableModal')).show();
    });

    // Tables grid click (inside modal)
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
            hideModal('tableModal');
            new bootstrap.Modal(document.getElementById('activeOrdersModal')).show();
        } else {
            selectAvailableTable(tableId, tableName);
            hideModal('tableModal');
        }
    });

    // Standard buttons (Load Table, New Order)
    document.getElementById('btn-load-table').addEventListener('click', () => {
        loadTable();
        hideModal('activeOrdersModal');
    });

    document.getElementById('btn-new-order-table').addEventListener('click', () => {
        selectAvailableTable(selectedTable.id, selectedTable.name);
        hideModal('activeOrdersModal');
    });

    // POS Actions
    document.getElementById('btn-save-order').addEventListener('click', () => saveOrder());
    document.getElementById('btn-assign-table').addEventListener('click', () => submitOrder(false));
    document.getElementById('btn-billing').addEventListener('click', () => setTableStatus(selectedTable.id, 'BILLING'));
    document.getElementById('btn-clear-cart').addEventListener('click', () => {
        if (confirm(t('messageConfirmClearOrder'))) {
            resetPOS();
        }
    });

    // Payment Logic
    document.getElementById('btn-proceed-payment').addEventListener('click', () => {
        if (isTableMode) {
            showPaymentModalForTable();
        } else {
            showPaymentModal();
        }
    });

    document.getElementById('btn-complete-order').addEventListener('click', () => submitOrder());

    // Payment Method Selection
    document.querySelectorAll('.payment-method-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.payment-method-btn').forEach(el => el.classList.remove('active'));
            btn.classList.add('active');
            const method = btn.dataset.method;
            document.getElementById('cash-payment-details').style.display = method === 'CASH' ? 'block' : 'none';
        });
    });

    // Quick Cash Buttons
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

    // Customization Modal
    document.getElementById('btn-add-with-customs').addEventListener('click', saveCustomizations);

    // Mobile UI
    document.getElementById('mobile-cart-toggle')?.addEventListener('click', toggleMobileCart);
    document.getElementById('btn-close-cart')?.addEventListener('click', toggleMobileCart);
    document.getElementById('cart-overlay')?.addEventListener('click', toggleMobileCart);
}

function toggleMobileCart() {
    const cartEl = document.querySelector('.pos-cart');
    const overlayEl = document.querySelector('.cart-overlay');
    cartEl.classList.toggle('active');
    overlayEl.classList.toggle('active');
}

function hideModal(id) {
    const modalEl = document.getElementById(id);
    if (!modalEl) return;
    const modal = bootstrap.Modal.getInstance(modalEl);
    if (modal) modal.hide();

    // FORCE BACKDROP REMOVAL
    setTimeout(() => {
        const backdrop = document.querySelector('.modal-backdrop');
        if (backdrop) {
            document.querySelectorAll('.modal-backdrop').forEach(el => el.remove());
            document.body.classList.remove('modal-open');
            document.body.style.paddingRight = '';
            document.body.style.overflow = '';
        }
    }, 150);
}

function calculateChange() {
    let total = 0;
    if (payingSingleOrderId) {
        const order = tableOrders.find(o => o.id === payingSingleOrderId);
        total = order ? order.total : 0;
    } else if (isTableMode) {
        tableOrders.forEach(o => {
            if (o.paymentStatus !== 'PAID' && o.status !== 'CANCELLED') {
                total += o.total;
            }
        });
    } else {
        total = calculateTotal();
    }
    const received = parseFloat(document.getElementById('received-amount').value || 0);
    const change = Math.max(0, received - total);
    document.getElementById('payment-change').textContent = formatPrice(change);
}

function selectAvailableTable(id, name) {
    selectedTable = { id, name };
    isTableMode = false;
    document.getElementById('selected-table-name').textContent = `${t('labelTableName')}: ${name}`;
    document.querySelectorAll('.table-btn').forEach(el => {
        el.classList.toggle('active', el.dataset.id === id);
    });

    document.getElementById('cart-actions-new').classList.remove('d-none');
    document.getElementById('cart-actions-edit').classList.add('d-none');
    document.getElementById('btn-assign-table').classList.remove('d-none');
    document.getElementById('btn-billing').classList.add('d-none');

    cart = [];
    updateCartUI();
}

async function loadTableOrders(tableId) {
    const list = document.getElementById('active-orders-list');
    if (!list) return;
    list.innerHTML = `<div class="text-center p-3 text-muted"><i class="fas fa-spinner fa-spin me-2"></i>${t('labelLoading')}</div>`;

    try {
        const res = await fetch(`/counter/fragments/active-orders/${tableId}`);
        if (res.ok) {
            list.innerHTML = await res.text();
        } else {
            throw new Error('Failed to load active orders');
        }
    } catch (e) {
        console.error(e);
        list.innerHTML = `<div class="alert alert-danger py-2 m-0 small">${t('messageOrderCreateError')}</div>`;
    }
}

async function loadTable() {
    if (!selectedTable) return;
    try {
        const res = await fetch(`/counter/api/tables/${selectedTable.id}/active-orders`);
        if (!res.ok) throw new Error('Failed to load orders');
        const orders = await res.json();
        isTableMode = true;
        tableOrders = orders.map(o => ({ ...o, expanded: false }));
        updateCartUI();
    } catch (error) {
        console.error('Error loading table orders:', error);
        showToast('Error loading table orders');
    }
}

async function refreshTableMode() {
    if (!selectedTable) {
        resetPOS();
        return;
    }
    const res = await fetch(`/counter/api/tables/${selectedTable.id}/active-orders`);
    const orders = await res.json();
    tableOrders = orders.map(o => {
        const old = tableOrders.find(prev => prev.id === o.id);
        return { ...o, expanded: old ? old.expanded : false };
    });
    updateCartUI();
}

async function renderTableCart() {
    const container = document.getElementById('cart-items');
    if (!container) return;

    if (tableOrders.length === 0) {
        isTableMode = false;
        updateCartUI();
        return;
    }

    try {
        const expandedIndices = tableOrders.map((o, idx) => o.expanded ? idx : null).filter(idx => idx !== null);
        const res = await fetch(`/counter/fragments/table-session-cart/${selectedTable.id}?expandedIndices=${expandedIndices.join(',')}`);
        if (res.ok) {
            container.innerHTML = await res.text();

            let totalUnpaid = 0;
            tableOrders.forEach(order => {
                if (order.paymentStatus !== 'PAID' && order.status !== 'CANCELLED') {
                    totalUnpaid += order.total;
                }
            });

            document.getElementById('cart-total').textContent = formatPrice(totalUnpaid);
            document.getElementById('cart-subtotal').textContent = formatPrice(totalUnpaid);
            document.getElementById('btn-proceed-payment').disabled = totalUnpaid <= 0;
            document.getElementById('btn-assign-table').classList.add('d-none');
            document.getElementById('btn-billing').classList.remove('d-none');
        }
    } catch (e) {
        console.error(e);
    }
}

function toggleOrderDetails(idx) {
    tableOrders[idx].expanded = !tableOrders[idx].expanded;
    renderTableCart();
}

function getStatusBadgeClass(paymentStatus, status) {
    if (status === 'CANCELLED') return 'bg-danger';
    if (paymentStatus === 'PAID') return 'bg-success';
    return 'bg-warning text-dark';
}

function getLocalizedStatus(paymentStatus, status) {
    if (status === 'CANCELLED') return t('labelCanceled');
    if (paymentStatus === 'PAID') return t('labelPaid');
    return t('labelUnpaid');
}

async function payIndividualOrder(orderId) {
    const order = tableOrders.find(o => o.id === orderId);
    if (!order) return;

    payingSingleOrderId = orderId;
    document.getElementById('payment-total').textContent = formatPrice(order.total);
    document.getElementById('received-amount').value = order.total.toFixed(2);
    calculateChange();

    new bootstrap.Modal(document.getElementById('paymentModal')).show();
}

async function cancelOrderFromTable(orderId) {
    if (!confirm(t('messageConfirmCancelOrder'))) return;
    try {
        const res = await fetch(`/counter/api/orders/${orderId}/cancel`, {
            method: 'POST',
            headers: { 'X-CSRF-TOKEN': csrfToken }
        });
        if (res.ok) {
            refreshTableMode();
            refreshTables();
        }
    } catch (e) {
        console.error(e);
    }
}

function editOrderFromTable(orderId) {
    const order = tableOrders.find(o => o.id === orderId);
    if (!order) return;

    editingOrderId = orderId;
    cart = order.items.map(item => ({
        id: item.id || btoa(Math.random()).substring(0, 8),
        productId: item.itemId,
        itemId: item.itemId,
        name: item.name,
        price: item.price,
        quantity: item.quantity,
        customizations: item.customizations || []
    }));

    isTableMode = false;
    document.getElementById('cart-actions-new').classList.add('d-none');
    document.getElementById('cart-actions-edit').classList.remove('d-none');
    updateCartUI();
}

async function openReassignModal(orderId) {
    reassigningOrderId = orderId;
    const container = document.getElementById('reassign-tables-list');
    if (!container) return;

    try {
        const currentTableId = selectedTable ? selectedTable.id : '';
        const res = await fetch(`/counter/fragments/reassign-tables?currentTableId=${currentTableId}`);
        if (res.ok) {
            container.innerHTML = await res.text();
            new bootstrap.Modal(document.getElementById('reassignModal')).show();
        }
    } catch (e) {
        console.error(e);
    }
}

async function reassignOrder(newTableId) {
    if (!reassigningOrderId) return;
    try {
        const res = await fetch(`/counter/api/orders/${reassigningOrderId}/reassign?tableId=${newTableId}`, {
            method: 'POST',
            headers: { 'X-CSRF-TOKEN': csrfToken }
        });
        if (res.ok) {
            hideModal('reassignModal');
            refreshTableMode();
            refreshTables();
        }
    } catch (e) {
        console.error(e);
    }
}

function showPaymentModal() {
    payingSingleOrderId = null;
    const total = calculateTotal();
    document.getElementById('payment-total').textContent = formatPrice(total);
    document.getElementById('received-amount').value = total.toFixed(2);
    calculateChange();
    new bootstrap.Modal(document.getElementById('paymentModal')).show();
}

async function showPaymentModalForTable() {
    payingSingleOrderId = null;
    let totalUnpaid = 0;
    tableOrders.forEach(o => {
        if (o.paymentStatus !== 'PAID' && o.status !== 'CANCELLED') {
            totalUnpaid += o.total;
        }
    });

    document.getElementById('payment-total').textContent = formatPrice(totalUnpaid);
    document.getElementById('received-amount').value = totalUnpaid.toFixed(2);
    calculateChange();

    new bootstrap.Modal(document.getElementById('paymentModal')).show();
}

async function submitTableBulkPayment() {
    const btn = document.getElementById('btn-complete-order');
    const originalHtml = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>${t('labelLoading')}`;

    try {
        const ordersToPay = payingSingleOrderId
            ? [tableOrders.find(o => o.id === payingSingleOrderId)]
            : tableOrders.filter(o => o.paymentStatus !== 'PAID' && o.status !== 'CANCELLED');

        const promises = ordersToPay.map(order =>
            fetch(`/counter/api/orders/${order.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify({ ...order, paid: true })
            })
        );

        const results = await Promise.all(promises);
        if (results.every(r => r.ok)) {
            // Only clear table if bulk payment or all orders now paid
            if (!payingSingleOrderId) {
                await fetch(`/counter/api/tables/${selectedTable.id}/status?status=AVAILABLE`, {
                    method: 'POST',
                    headers: { 'X-CSRF-TOKEN': csrfToken }
                });
            }

            showToast(t('labelPaid'));
            hideModal('paymentModal');

            if (payingSingleOrderId) {
                payingSingleOrderId = null;
                refreshTableMode();
                refreshTables();
            } else {
                resetPOS();
            }
        } else {
            showToast('Error processing status update');
        }
    } catch (e) {
        console.error(e);
        showToast('Error processing payment');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalHtml;
    }
}

async function submitOrder(paid = true) {
    if (isTableMode && paid) {
        await submitTableBulkPayment();
        return;
    }

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
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>${t('labelLoading')}`;

    try {
        const url = editingOrderId ? `/counter/api/orders/${editingOrderId}` : '/counter/api/order';
        const method = editingOrderId ? 'PUT' : 'POST';

        const res = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(request)
        });

        if (res.ok) {
            const data = await res.json();
            if (request.tableId) {
                await fetch(`/counter/api/tables/${request.tableId}/status?status=OCCUPIED`, {
                    method: 'POST',
                    headers: { 'X-CSRF-TOKEN': csrfToken }
                });
            }

            showToast(t('messageOrderCreatedSuccess').replace('{0}', data.orderNumber || editingOrderId));
            if (paid) hideModal('paymentModal');
            resetPOS();
        } else {
            showToast(t('messageOrderCreateError'));
        }
    } catch (error) {
        console.error('Error submitting order:', error);
        showToast(t('messageOrderSubmitError'));
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalHtml;
    }
}

async function saveOrder() {
    await submitOrder(false);
}

function resetPOS() {
    cart = [];
    selectedTable = null;
    editingOrderId = null;
    isTableMode = false;
    tableOrders = [];
    document.getElementById('selected-table-name').textContent = t('labelAssignTable');
    document.getElementById('cart-actions-new').classList.remove('d-none');
    document.getElementById('cart-actions-edit').classList.add('d-none');
    document.getElementById('btn-billing').classList.add('d-none');
    document.getElementById('btn-assign-table').classList.remove('d-none');
    updateCartUI();
    refreshTables();
}

async function refreshTables() {
    try {
        const tableRes = await fetch('/api/backoffice/tables');
        if (tableRes.ok) {
            const data = await tableRes.json();
            allTables = data.map(d => ({
                id: d.id,
                ...d.customFields
            }));
            renderTables();
        }
    } catch (error) {
        console.error('Error refreshing tables:', error);
    }
}

async function renderTables() {
    const list = document.getElementById('tables-list');
    if (!list) return;

    try {
        const selectedTableId = selectedTable ? selectedTable.id : '';
        const res = await fetch(`/counter/fragments/tables?selectedTableId=${selectedTableId}`);
        if (res.ok) {
            list.innerHTML = await res.text();
        }
    } catch (error) {
        console.error('Error refreshing tables:', error);
    }
}

function renderAll() {
    renderCategories();
    renderProducts();
    updateCartUI();
}

async function renderCategories() {
    const sidebar = document.getElementById('categories-sidebar');
    if (!sidebar) return;

    try {
        const res = await fetch(`/counter/fragments/categories?selectedGroup=${selectedGroup}`);
        if (res.ok) {
            sidebar.innerHTML = await res.text();
        }
    } catch (e) {
        console.error(e);
    }
}

async function renderProducts(filter = '') {
    const grid = document.getElementById('products-grid');
    if (!grid) return;

    try {
        const res = await fetch(`/counter/fragments/products?groupId=${selectedGroup}&filter=${encodeURIComponent(filter)}`);
        if (res.ok) {
            grid.innerHTML = await res.text();
        }
    } catch (e) {
        console.error(e);
    }
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
    if (window.innerWidth < 992) toggleMobileCart(); // Open cart on mobile when item added
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

async function updateCartUI() {
    if (isTableMode) {
        renderTableCart();
        return;
    }

    const container = document.getElementById('cart-items');
    if (!container) return;

    try {
        const res = await fetch('/counter/fragments/order-cart', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(cart)
        });
        if (res.ok) {
            container.innerHTML = await res.text();

            const total = calculateTotal();
            document.getElementById('cart-subtotal').textContent = formatPrice(total);
            document.getElementById('cart-total').textContent = formatPrice(total);

            const mobileBadge = document.getElementById('mobile-cart-count');
            const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
            if (mobileBadge) {
                mobileBadge.textContent = totalItems;
                mobileBadge.classList.toggle('d-none', totalItems === 0);
            }

            document.getElementById('btn-proceed-payment').disabled = cart.length === 0;
            document.getElementById('btn-assign-table').disabled = !selectedTable || cart.length === 0;
            document.getElementById('btn-assign-table').classList.remove('d-none');
            document.getElementById('btn-billing').classList.add('d-none');
        }
    } catch (e) {
        console.error(e);
    }
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
    return parseFloat(val).toFixed(2);
}

async function showCustomizationModal(product, currentCustomizations = []) {
    currentProduct = product;
    document.getElementById('product-modal-name').textContent = `${t('customizerTitle')} ${product.name}`;
    const optionsContainer = document.getElementById('customization-options');
    if (!optionsContainer) return;

    if (!product.customizations) return;

    try {
        const selectedIds = currentCustomizations.map(c => c.id).join(',');
        const res = await fetch(`/counter/fragments/customization-options/${product.id}?selected=${selectedIds}`);
        if (res.ok) {
            optionsContainer.innerHTML = await res.text();
            new bootstrap.Modal(document.getElementById('customizationModal')).show();
        }
    } catch (e) {
        console.error(e);
    }
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
    hideModal('customizationModal');
}

async function setTableStatus(tableId, status) {
    if (!tableId) return;
    try {
        const res = await fetch(`/counter/api/tables/${tableId}/status?status=${status}`, {
            method: 'POST',
            headers: { 'X-CSRF-TOKEN': csrfToken }
        });
        if (res.ok) {
            showToast(t(`messageTableStatus${status.charAt(0).toUpperCase() + status.slice(1).toLowerCase()}`));
            resetPOS();
        }
    } catch (error) {
        console.error('Error updating table status:', error);
    }
}

function showToast(message) {
    const toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) return;

    fetch('/api/toast', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        },
        body: JSON.stringify({ message: message })
    })
        .then(response => response.text())
        .then(html => {
            const div = document.createElement('div');
            div.innerHTML = html;
            const toast = div.firstElementChild;
            toastContainer.appendChild(toast);

            setTimeout(() => {
                if (toast.parentNode) toast.parentNode.removeChild(toast);
            }, 3000);
        })
        .catch(error => console.error('Error:', error));
}
