// Data storage
let groups = [];
let customizations = [];
let products = [];
let tables = [];
let allPaymentConfigs = [];
let selectedPaymentConfig = null;
let currentSection = 'groups';
let editingId = null;
let selectedProductsForGroup = [];
let selectedCustomizationsForProduct = [];
let i18n = {};

// CSRF token helper
function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]')?.content || '';
}

async function fetchFragment(url, payload) {
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': getCsrfToken()
        },
        body: JSON.stringify(payload)
    });
    return await response.text();
}

// Load i18n data from the HTML
function loadI18n() {
    const el = document.getElementById('i18n-backoffice');
    if (!el) return;

    // Copy all dataset properties to i18n object
    for (const key in el.dataset) {
        i18n[key] = el.dataset[key];
    }
}

// Initialize
// Initialize
document.addEventListener('DOMContentLoaded', function () {
    loadI18n();
    loadData();

    // Setup form handlers
    document.getElementById('groupFormElement').addEventListener('submit', handleGroupSubmit);
    document.getElementById('customizationFormElement').addEventListener('submit', handleCustomizationSubmit);
    document.getElementById('productFormElement').addEventListener('submit', handleProductSubmit);
    document.getElementById('tableFormElement').addEventListener('submit', handleTableSubmit);

    // Payment config doesn't use standard submit if it's dynamic, 
    // but in my fragment I used a form with id paymentConfigForm
});

async function handlePaymentConfigSubmit(e) {
    if (e) e.preventDefault();
    const modes = [];
    if (document.getElementById('mode-cash').checked) modes.push('CASH');
    if (document.getElementById('mode-card').checked) modes.push('CARD');

    const images = Array.from(document.querySelectorAll('input[name="moneyImages"]')).map(i => i.value);

    try {
        const res = await fetch('/api/backoffice/payment', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': getCsrfToken()
            },
            body: JSON.stringify({ activeModes: modes, moneyImages: images })
        });
        if (res.ok) {
            alert('Payment configuration saved successfully!');
            loadData();
        }
    } catch (error) {
        console.error('Error saving payment config:', error);
    }
}

// Load data from backend
async function loadData() {
    try {
        // Load products
        const productsResponse = await fetch('/api/backoffice/products');
        if (productsResponse.ok) {
            const productsData = await productsResponse.json();
            products = productsData.map(data => ({
                id: data.id,
                name: data.customFields.name,
                price: data.customFields.price,
                description: data.customFields.description,
                image: data.customFields.image,
                customizations: data.customFields.customizations || [],
                active: data.customFields.active !== false
            }));
        }

        // Load customizations
        const customizationsResponse = await fetch('/api/backoffice/customizations');
        if (customizationsResponse.ok) {
            const customizationsData = await customizationsResponse.json();
            customizations = customizationsData.map(data => ({
                id: data.id,
                name: data.customFields.name,
                type: data.customFields.type,
                options: data.customFields.options || [],
                usageCount: data.customFields.usageCount || 0
            }));
        }

        // Load groups
        const groupsResponse = await fetch('/api/backoffice/groups');
        if (groupsResponse.ok) {
            const groupsData = await groupsResponse.json();
            groups = groupsData.map(data => ({
                id: data.id,
                name: data.customFields.name,
                description: data.customFields.description,
                icon: data.customFields.icon,
                products: data.customFields.products || []
            }));
        }

        // Load tables
        const tablesResponse = await fetch('/api/backoffice/tables');
        if (tablesResponse.ok) {
            const tablesData = await tablesResponse.json();
            tables = tablesData.map(data => ({
                id: data.id,
                name: data.customFields.name,
                seats: data.customFields.seats,
                status: data.customFields.status,
                active: data.customFields.active
            }));
        }

        // Load payment configs
        const paymentResponse = await fetch('/api/backoffice/payment/all');
        if (paymentResponse.ok) {
            allPaymentConfigs = await paymentResponse.json();
            // Default to first or active one if not selected
            if (!selectedPaymentConfig && allPaymentConfigs.length > 0) {
                selectedPaymentConfig = allPaymentConfigs.find(c => c.active) || allPaymentConfigs[0];
            } else if (selectedPaymentConfig) {
                // Refresh the selected one from the list
                selectedPaymentConfig = allPaymentConfigs.find(c => c.id === selectedPaymentConfig.id) || allPaymentConfigs[0];
            }
        }

        renderAll();
    } catch (error) {
        console.error('Error loading data:', error);
    }
}

// Render all sections
function renderAll() {
    renderGroupsList();
    renderCustomizationsList();
    renderProductsList();
    renderTablesList();
    renderPaymentConfig();
    renderQuickViews();
    updateCounts();
}

// Show section
function showSection(section) {
    currentSection = section;

    // Hide all sections
    document.querySelectorAll('.section-content').forEach(el => el.style.display = 'none');

    // Show selected section
    document.getElementById(`${section}-section`).style.display = 'block';

    // Update navigation
    document.querySelectorAll('.list-group-item').forEach(el => el.classList.remove('active'));
    document.getElementById(`nav-${section}`).classList.add('active');

    // Update quick view
    document.querySelectorAll('.quick-view-section').forEach(el => el.style.display = 'none');
    const quickView = document.getElementById(`${section}-quick-view`);
    if (quickView) quickView.style.display = 'block';
}

// Toggle side view
function toggleSideView() {
    const sideView = document.getElementById('side-view');
    const mainView = sideView.previousElementSibling;

    if (sideView.style.display === 'none') {
        sideView.style.display = 'block';
        mainView.classList.remove('col-md-10');
        mainView.classList.add('col-md-7');
    } else {
        sideView.style.display = 'none';
        mainView.classList.remove('col-md-7');
        mainView.classList.add('col-md-10');
    }
}

// Create new (based on current section)
function createNew() {
    if (currentSection === 'groups') showGroupForm();
    else if (currentSection === 'customizations') showCustomizationForm();
    else if (currentSection === 'products') showProductForm();
    else if (currentSection === 'tables') showTableForm();
}

// Update counts
function updateCounts() {
    document.getElementById('groups-count').textContent = groups.length;
    document.getElementById('customizations-count').textContent = customizations.length;
    document.getElementById('products-count').textContent = products.length;
    const tablesCount = document.getElementById('tables-count');
    if (tablesCount) tablesCount.textContent = tables.length;
}

// Cancel form
function cancelForm() {
    document.getElementById('group-form').style.display = 'none';
    document.getElementById('customization-form').style.display = 'none';
    document.getElementById('product-form').style.display = 'none';
    const preview = document.getElementById('product-image-preview');
    if (preview) preview.style.display = 'none';
    const tableList = document.getElementById('tables-list');
    const tableFormContainer = document.getElementById('table-form-container');
    if (tableList) tableList.style.display = 'block';
    if (tableFormContainer) tableFormContainer.innerHTML = '';
    editingId = null;
}

// === GROUPS MANAGEMENT ===

async function renderGroupsList() {
    const container = document.getElementById('groups-list');
    container.innerHTML = await fetchFragment('/api/backoffice/fragments/groups-list', groups);
}

function showGroupForm(groupId = null) {
    editingId = groupId;
    const form = document.getElementById('group-form');
    const title = document.getElementById('group-form-title');

    if (groupId) {
        const group = groups.find(g => g.id === groupId);
        title.textContent = 'Edit Group';
        document.getElementById('group-id').value = group.id;
        document.getElementById('group-name').value = group.name;
        document.getElementById('group-description').value = group.description || '';
        document.getElementById('group-icon').value = group.icon || '';
        selectedProductsForGroup = [...group.products];
        updateGroupProductsList();
    } else {
        title.textContent = 'Create New Group';
        document.getElementById('groupFormElement').reset();
        selectedProductsForGroup = [];
        updateGroupProductsList();
    }

    form.style.display = 'block';
    form.scrollIntoView({ behavior: 'smooth' });
}

async function handleGroupSubmit(e) {
    e.preventDefault();

    const groupData = {
        name: document.getElementById('group-name').value,
        description: document.getElementById('group-description').value,
        icon: document.getElementById('group-icon').value,
        products: selectedProductsForGroup
    };

    try {
        let response;
        if (editingId) {
            response = await fetch(`/api/backoffice/groups/${editingId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken()
                },
                body: JSON.stringify(groupData)
            });
        } else {
            response = await fetch('/api/backoffice/groups', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken()
                },
                body: JSON.stringify(groupData)
            });
        }

        if (response.ok) {
            await loadData();
            cancelForm();
            await loadData();
            cancelForm();
            showToast(i18n.groupsSaveSuccess);
        } else {
            showToast(i18n.groupsSaveError, 'error');
        }
    } catch (error) {
        console.error('Error saving group:', error);
        showToast('Error saving group', 'error');
    }
}

function editGroup(id) {
    showGroupForm(id);
}

async function deleteGroup(id) {
    if (confirm(i18n.groupsDeleteConfirm)) {
        try {
            const response = await fetch(`/api/backoffice/groups/${id}`, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': getCsrfToken()
                }
            });

            if (response.ok) {
                await loadData();
                showToast('Group deleted successfully!');
            } else {
                showToast('Error deleting group', 'error');
            }
        } catch (error) {
            console.error('Error deleting group:', error);
            showToast('Error deleting group', 'error');
        }
    }
}

async function updateGroupProductsList() {
    const container = document.getElementById('group-products-list');
    const selectedProducts = selectedProductsForGroup.map(id => products.find(p => p.id === id)).filter(p => p);
    container.innerHTML = await fetchFragment('/api/backoffice/fragments/group-products-badges', selectedProducts);
}

function removeProductFromGroup(productId) {
    selectedProductsForGroup = selectedProductsForGroup.filter(id => id !== productId);
    updateGroupProductsList();
}

async function openProductSelector(context) {
    // Populate product selector modal
    const container = document.getElementById('product-selector-list');
    container.innerHTML = await fetchFragment('/api/backoffice/fragments/selector-list', { items: products, selectedIds: selectedProductsForGroup });

    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('productSelectorModal'));
    modal.show();
}

function confirmProductSelection() {
    // Get selected products
    const checkboxes = document.querySelectorAll('#product-selector-list input[type="checkbox"]:checked');
    selectedProductsForGroup = Array.from(checkboxes).map(cb => cb.value);
    updateGroupProductsList();

    // Close modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('productSelectorModal'));
    modal.hide();
}

// === CUSTOMIZATIONS MANAGEMENT ===

async function renderCustomizationsList() {
    const container = document.getElementById('customizations-list');
    container.innerHTML = await fetchFragment('/api/backoffice/fragments/customizations-list', customizations);
}

function showCustomizationForm(customId = null) {
    editingId = customId;
    const form = document.getElementById('customization-form');
    const title = document.getElementById('customization-form-title');

    if (customId) {
        const custom = customizations.find(c => c.id === customId);
        title.textContent = 'Edit Customization';
        document.getElementById('customization-id').value = custom.id;
        document.getElementById('customization-name').value = custom.name;
        document.getElementById('customization-type').value = custom.type;
        renderCustomizationOptions(custom.options);
    } else {
        title.textContent = 'Create New Customization';
        document.getElementById('customizationFormElement').reset();
        renderCustomizationOptions([]);
    }

    form.style.display = 'block';
    form.scrollIntoView({ behavior: 'smooth' });
}

async function renderCustomizationOptions(options = []) {
    const container = document.getElementById('customization-options-container');

    if (options.length === 0) {
        container.innerHTML = `<p class="text-muted">${i18n.customizationsOptionsEmptyHint}</p>`;
        return;
    }

    let html = '';
    for (let i = 0; i < options.length; i++) {
        html += await fetchFragment('/api/backoffice/fragments/customization-option', { opt: options[i], index: i });
    }
    container.innerHTML = html;
}

async function addCustomizationOption() {
    const container = document.getElementById('customization-options-container');
    const index = container.querySelectorAll('.card').length;

    // Remove placeholder text if this is the first option
    if (index === 0) {
        const placeholder = container.querySelector('p.text-muted');
        if (placeholder) {
            placeholder.remove();
        }
    }

    const optionHtml = await fetchFragment('/api/backoffice/fragments/customization-option', { opt: null, index: index });
    container.insertAdjacentHTML('beforeend', optionHtml);
}

function removeCustomizationOption(index) {
    const container = document.getElementById('customization-options-container');
    const options = container.querySelectorAll('.card');
    if (options[index]) {
        options[index].remove();
    }
}

async function handleCustomizationSubmit(e) {
    e.preventDefault();

    // Collect options
    const optionsContainer = document.getElementById('customization-options-container');
    const optionElements = optionsContainer.querySelectorAll('.card');
    const options = Array.from(optionElements).map((el, index) => {
        const name = document.getElementById(`opt-name-${index}`)?.value || '';
        const price = document.getElementById(`opt-price-${index}`)?.value || '0';
        const defaultValue = document.getElementById(`opt-defaultValue-${index}`)?.value || '1';
        const isSelectedByDefault = document.getElementById(`opt-isSelectedByDefault-${index}`)?.checked || false;

        return {
            name,
            price: parseFloat(price),
            defaultValue: parseInt(defaultValue),
            isSelectedByDefault
        };
    }).filter(opt => opt.name.trim() !== '');

    const customizationData = {
        name: document.getElementById('customization-name').value,
        type: document.getElementById('customization-type').value,
        options: options,
        usageCount: 0
    };

    try {
        let response;
        if (editingId) {
            response = await fetch(`/api/backoffice/customizations/${editingId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken()
                },
                body: JSON.stringify(customizationData)
            });
        } else {
            response = await fetch('/api/backoffice/customizations', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken()
                },
                body: JSON.stringify(customizationData)
            });
        }

        if (response.ok) {
            await loadData();
            cancelForm();
            await loadData();
            cancelForm();
            showToast(i18n.customizationsSaveSuccess);
        } else {
            showToast(i18n.customizationsSaveError, 'error');
        }
    } catch (error) {
        console.error('Error saving customization:', error);
        showToast(i18n.customizationsSaveError, 'error');
    }
}

function editCustomization(id) {
    showCustomizationForm(id);
}

async function deleteCustomization(id) {
    if (confirm(i18n.customizationsDeleteConfirm)) {
        try {
            const response = await fetch(`/api/backoffice/customizations/${id}`, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': getCsrfToken()
                }
            });

            if (response.ok) {
                await loadData();
                showToast(i18n.customizationsDeleteSuccess);
            } else {
                showToast(i18n.customizationsDeleteError, 'error');
            }
        } catch (error) {
            console.error('Error deleting customization:', error);
            showToast(i18n.customizationsDeleteError, 'error');
        }
    }
}

// === PRODUCTS MANAGEMENT ===

async function renderProductsList() {
    const container = document.getElementById('products-list');
    container.innerHTML = await fetchFragment('/api/backoffice/fragments/products-list', products);
}

function showProductForm(productId = null) {
    editingId = productId;
    const form = document.getElementById('product-form');
    const title = document.getElementById('product-form-title');

    if (productId) {
        const product = products.find(p => p.id === productId);
        title.textContent = 'Edit Product';
        document.getElementById('product-id').value = product.id;
        document.getElementById('product-name').value = product.name;
        document.getElementById('product-price').value = product.price;
        document.getElementById('product-description').value = product.description;
        document.getElementById('product-image').value = product.image || '';
        const preview = document.getElementById('product-image-preview');
        const previewImg = document.getElementById('product-image-preview-img');
        if (product.image) {
            if (previewImg) previewImg.src = product.image;
            if (preview) preview.style.display = 'block';
        } else {
            if (preview) preview.style.display = 'none';
        }
        selectedCustomizationsForProduct = product.customizations ? [...product.customizations] : [];
        updateProductCustomizationsList();
    } else {
        title.textContent = 'Create New Product';
        document.getElementById('productFormElement').reset();
        const preview = document.getElementById('product-image-preview');
        if (preview) preview.style.display = 'none';
        selectedCustomizationsForProduct = [];
        updateProductCustomizationsList();
    }

    form.style.display = 'block';
    form.scrollIntoView({ behavior: 'smooth' });
}

async function handleProductSubmit(e) {
    e.preventDefault();

    const productData = {
        name: document.getElementById('product-name').value,
        price: parseFloat(document.getElementById('product-price').value),
        description: document.getElementById('product-description').value,
        image: document.getElementById('product-image').value,
        customizations: selectedCustomizationsForProduct,
        active: document.getElementById('product-active').checked
    };

    try {
        let response;
        if (editingId) {
            response = await fetch(`/api/backoffice/products/${editingId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken()
                },
                body: JSON.stringify(productData)
            });
        } else {
            response = await fetch('/api/backoffice/products', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken()
                },
                body: JSON.stringify(productData)
            });
        }

        if (response.ok) {
            await loadData();
            cancelForm();
            showToast(i18n.productsSaveSuccess);
        } else {
            showToast(i18n.productsSaveError, 'error');
        }
    } catch (error) {
        console.error('Error saving product:', error);
        showToast(i18n.productsSaveError, 'error');
    }
}

function editProduct(id) {
    showProductForm(id);
}

async function deleteProduct(id) {
    if (confirm(i18n.productsDeleteConfirm)) {
        try {
            const response = await fetch(`/api/backoffice/products/${id}`, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': getCsrfToken()
                }
            });

            if (response.ok) {
                await loadData();
                showToast(i18n.productsDeleteSuccess);
            } else {
                showToast(i18n.productsDeleteError, 'error');
            }
        } catch (error) {
            console.error('Error deleting product:', error);
            showToast(i18n.productsDeleteError, 'error');
        }
    }
}

async function updateProductCustomizationsList() {
    const container = document.getElementById('product-customizations-list');
    const selectedCustomizations = selectedCustomizationsForProduct.map(id => customizations.find(c => c.id === id)).filter(c => c);
    container.innerHTML = await fetchFragment('/api/backoffice/fragments/product-customizations-badges', selectedCustomizations);
}

function removeCustomizationFromProduct(customizationId) {
    selectedCustomizationsForProduct = selectedCustomizationsForProduct.filter(id => id !== customizationId);
    updateProductCustomizationsList();
}

async function openCustomizationSelector() {
    // Populate customization selector modal
    const container = document.getElementById('customization-selector-list');
    container.innerHTML = await fetchFragment('/api/backoffice/fragments/selector-list', { items: customizations, selectedIds: selectedCustomizationsForProduct });

    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('customizationSelectorModal'));
    modal.show();
}

function confirmCustomizationSelection() {
    // Get selected customizations
    const checkboxes = document.querySelectorAll('#customization-selector-list input[type="checkbox"]:checked');
    selectedCustomizationsForProduct = Array.from(checkboxes).map(cb => cb.value);
    updateProductCustomizationsList();

    // Close modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('customizationSelectorModal'));
    modal.hide();
}

function showCustomizationFormPopup() {
    // Show quick customization modal
    const modal = new bootstrap.Modal(document.getElementById('quickCustomizationModal'));
    modal.show();
}

async function saveQuickCustomization() {
    const name = document.getElementById('quick-customization-name').value;
    const type = document.getElementById('quick-customization-type').value;

    if (!name || !type) {
        showToast(i18n.customizationsValidationError, 'error');
        return;
    }

    const customizationData = {
        name: name,
        type: type,
        options: [],
        usageCount: 0
    };

    try {
        const response = await fetch('/api/backoffice/customizations', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': getCsrfToken()
            },
            body: JSON.stringify(customizationData)
        });

        if (response.ok) {
            await loadData();

            // Close modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('quickCustomizationModal'));
            modal.hide();

            // Clear form
            document.getElementById('quick-customization-name').value = '';
            document.getElementById('quick-customization-type').value = 'radio';

            showToast('Customization created successfully!');
        } else {
            showToast('Error creating customization', 'error');
        }
    } catch (error) {
        console.error('Error creating customization:', error);
        showToast('Error creating customization', 'error');
    }
}

// === QUICK VIEWS ===

async function renderQuickViews() {
    // Groups quick view
    const groupsQuickView = document.getElementById('groups-quick-view');
    groupsQuickView.innerHTML = await fetchFragment('/api/backoffice/fragments/quick-view', { data: groups, type: 'groups' });

    // Customizations quick view
    const customizationsQuickView = document.getElementById('customizations-quick-view');
    customizationsQuickView.innerHTML = await fetchFragment('/api/backoffice/fragments/quick-view', { data: customizations, type: 'customizations' });

    // Products quick view
    const productsQuickView = document.getElementById('products-quick-view');
    productsQuickView.innerHTML = await fetchFragment('/api/backoffice/fragments/quick-view', { data: products, type: 'products' });
}

// === UTILITY FUNCTIONS ===

function showToast(message, type = 'success') {
    const toastElement = document.getElementById('notification-toast');
    const toastBody = document.getElementById('toast-message');

    // Set the message
    toastBody.textContent = message;

    // Set the color based on type
    toastElement.classList.remove('bg-success', 'bg-danger', 'bg-warning', 'text-white');
    if (type === 'success') {
        toastElement.classList.add('bg-success', 'text-white');
    } else if (type === 'error') {
        toastElement.classList.add('bg-danger', 'text-white');
    } else if (type === 'warning') {
        toastElement.classList.add('bg-warning', 'text-dark');
    }

    // Show the toast
    const toast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: 2000  // Auto-dismiss after 2 seconds
    });
    toast.show();
}

// === IMAGE MANAGEMENT ===

let imageSelectionCallback = null;

/**
 * Open the image selector modal
 * @param {Function} callback Optional callback to handle selection
 */
function openImageSelector(callback = null) {
    imageSelectionCallback = callback;
    const modal = new bootstrap.Modal(document.getElementById('imageSelectorModal'));

    // Load images when modal opens
    loadSystemImages();
    loadUserImages();

    // Setup upload form handler
    const uploadForm = document.getElementById('imageUploadForm');
    if (uploadForm) {
        uploadForm.onsubmit = handleImageUpload;
    }

    // Setup file input preview
    const fileInput = document.getElementById('imageFile');
    if (fileInput) {
        fileInput.onchange = previewUploadImage;
    }

    modal.show();
}

/**
 * Load system images from classpath
 */
async function loadSystemImages() {
    const container = document.getElementById('system-images-container');

    try {
        const response = await fetch('/api/backoffice/images/system');
        if (!response.ok) {
            container.innerHTML = '<div class="col-12 text-center text-muted py-5"><p>No system images found</p></div>';
            return;
        }

        const imagesByFolder = await response.json();
        renderImageGallery(imagesByFolder, container, 'system');
    } catch (error) {
        console.error('Error loading system images:', error);
        container.innerHTML = '<div class="col-12 text-center text-danger py-5"><p>Error loading system images</p></div>';
    }
}

/**
 * Load user-uploaded images
 */
async function loadUserImages() {
    const container = document.getElementById('user-images-container');

    try {
        const response = await fetch('/api/backoffice/images/user');
        if (!response.ok) {
            container.innerHTML = '<div class="col-12 text-center text-muted py-5"><p>No uploaded images yet</p></div>';
            return;
        }

        const imagesByFolder = await response.json();
        if (Object.keys(imagesByFolder).length === 0) {
            container.innerHTML = '<div class="col-12 text-center text-muted py-5"><p>No uploaded images yet. Upload your first image!</p></div>';
            return;
        }

        renderImageGallery(imagesByFolder, container, 'user');
    } catch (error) {
        console.error('Error loading user images:', error);
        container.innerHTML = '<div class="col-12 text-center text-danger py-5"><p>Error loading user images</p></div>';
    }
}

/**
 * Render image gallery grouped by folders
 */
async function renderImageGallery(imagesByFolder, container, type) {
    // Pre-format sizes for Thymeleaf
    const formattedImagesByFolder = {};
    for (const [folder, images] of Object.entries(imagesByFolder)) {
        formattedImagesByFolder[folder] = images.map(img => ({
            ...img,
            formattedSize: formatFileSize(img.size)
        }));
    }

    container.innerHTML = await fetchFragment('/api/backoffice/fragments/image-gallery', { imagesByFolder: formattedImagesByFolder, type: type });
}

/**
 * Select an image
 */
function selectImage(imageUrl) {
    if (imageSelectionCallback) {
        imageSelectionCallback(imageUrl);
    } else {
        // Default product form behavior
        document.getElementById('product-image').value = imageUrl;

        const preview = document.getElementById('product-image-preview');
        const previewImg = document.getElementById('product-image-preview-img');
        if (previewImg) previewImg.src = imageUrl;
        if (preview) preview.style.display = 'block';
    }

    // Close modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('imageSelectorModal'));
    if (modal) modal.hide();

    showToast('Image selected successfully!', 'success');
}

/**
 * Preview image before upload
 */
function previewUploadImage(event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            document.getElementById('imagePreview').src = e.target.result;
            document.getElementById('imagePreviewContainer').style.display = 'block';
        };
        reader.readAsDataURL(file);
    }
}

/**
 * Handle image upload
 */
async function handleImageUpload(event) {
    event.preventDefault();

    const fileInput = document.getElementById('imageFile');
    const folderInput = document.getElementById('imageFolder');

    if (!fileInput.files || fileInput.files.length === 0) {
        showToast('Please select a file', 'error');
        return;
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    if (folderInput.value.trim()) {
        formData.append('folder', folderInput.value.trim());
    }

    try {
        const response = await fetch('/api/backoffice/images/upload', {
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': getCsrfToken()
            },
            body: formData
        });

        if (!response.ok) {
            const error = await response.json();
            showToast(error.error || 'Upload failed', 'error');
            return;
        }

        const imageInfo = await response.json();

        // Select the newly uploaded image
        selectImage(imageInfo.url);

        // Reset form
        document.getElementById('imageUploadForm').reset();
        document.getElementById('imagePreviewContainer').style.display = 'none';

        // Reload user images
        loadUserImages();

        showToast('Image uploaded successfully!', 'success');
    } catch (error) {
        console.error('Error uploading image:', error);
        showToast('Error uploading image', 'error');
    }
}

/**
 * Format file size for display
 */
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}

// === TRANSLATION MANAGEMENT ===

/**
 * Navigate to group translations page
 */
function manageGroupTranslations(groupId) {
    window.location.assign(`/backoffice/translations/groups/${groupId}`);
}

/**
 * Navigate to product translations page
 */
function manageProductTranslations(productId) {
    window.location.assign(`/backoffice/translations/products/${productId}`);
}

/**
 * Navigate to customization translations page
 */
function manageCustomizationTranslations(customizationId) {
    window.location.assign(`/backoffice/translations/customizations/${customizationId}`);
}

/**
 * Navigate to table translations page
 */
function manageTableTranslations(tableId) {
    window.location.assign(`/backoffice/translations/tables/${tableId}`);
}

// === TABLES MANAGEMENT ===

async function renderTablesList() {
    const list = document.getElementById('tables-list');
    if (!list) return;
    list.innerHTML = await fetchFragment('/api/backoffice/fragments/tables-list', tables);
}

async function showTableForm(tableId = null) {
    editingId = tableId;
    const container = document.getElementById('table-form-container');
    const list = document.getElementById('tables-list');

    // Hide list while editing
    list.style.display = 'none';

    let table = null;
    if (tableId) {
        table = tables.find(t => t.id === tableId);
    }

    container.innerHTML = await fetchFragment('/api/backoffice/fragments/table-form', {
        table: table,
        statuses: ['AVAILABLE', 'OCCUPIED', 'BILLING']
    });

    // Populate fields if editing
    if (table) {
        document.getElementById('table-form-title').textContent = 'Edit Table';
        document.getElementById('table-id').value = table.id;
        document.getElementById('table-name').value = table.name;
        document.getElementById('table-seats').value = table.seats;
        document.getElementById('table-status').value = table.status;
        document.getElementById('table-active').checked = table.active;
    }

    // Add submit listener
    const form = document.getElementById('tableFormElement');
    if (form) {
        form.addEventListener('submit', handleTableSubmit);
    }

    container.scrollIntoView({ behavior: 'smooth' });
}

async function handleTableSubmit(e) {
    e.preventDefault();

    const id = document.getElementById('table-id').value;
    const tableData = {
        name: document.getElementById('table-name').value,
        seats: parseInt(document.getElementById('table-seats').value),
        status: document.getElementById('table-status').value,
        active: document.getElementById('table-active').checked
    };

    try {
        const url = id ? `/api/backoffice/tables/${id}` : '/api/backoffice/tables';
        const method = id ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': getCsrfToken()
            },
            body: JSON.stringify(tableData)
        });

        if (response.ok) {
            await loadData();
            cancelForm();
            showToast('Table saved successfully!');
        } else {
            showToast('Error saving table', 'error');
        }
    } catch (error) {
        console.error('Error saving table:', error);
        showToast('Error saving table', 'error');
    }
}

function editTable(id) {
    showTableForm(id);
}

async function deleteTable(id) {
    if (confirm('Are you sure you want to delete this table?')) {
        try {
            const response = await fetch(`/api/backoffice/tables/${id}`, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': getCsrfToken()
                }
            });

            if (response.ok) {
                await loadData();
                showToast('Table deleted successfully!');
            } else {
                showToast('Error deleting table', 'error');
            }
        } catch (error) {
            console.error('Error deleting table:', error);
            showToast('Error deleting table', 'error');
        }
    }
}

// === PAYMENT MANAGEMENT ===

async function renderPaymentConfig() {
    const container = document.getElementById('payment-config-container');
    if (!container) return;

    container.innerHTML = await fetchFragment('/api/backoffice/fragments/payment-section', {
        paymentConfig: selectedPaymentConfig,
        allConfigs: allPaymentConfigs
    });

    // Handle form submission
    const form = document.getElementById('paymentConfigForm');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = document.getElementById('payment-config-id').value;
            const modes = [];
            if (document.getElementById('mode-cash').checked) modes.push('CASH');
            if (document.getElementById('mode-card').checked) modes.push('CARD');

            const configData = {
                id: id,
                activeModes: modes,
                moneyDenominations: selectedPaymentConfig.moneyDenominations || [],
                active: selectedPaymentConfig.active || false
            };

            try {
                const response = await fetch('/api/backoffice/payment', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': getCsrfToken()
                    },
                    body: JSON.stringify(configData)
                });

                if (response.ok) {
                    showToast('Payment configuration saved successfully!');
                    await loadData();
                    // Keep the saved one selected
                    selectedPaymentConfig = allPaymentConfigs.find(c => c.id === id);
                    renderPaymentConfig();
                } else {
                    showToast('Error saving payment configuration', 'error');
                }
            } catch (error) {
                console.error('Error saving payment config:', error);
                showToast('Error saving payment config', 'error');
            }
        });
    }
}

function selectPaymentConfig(id) {
    selectedPaymentConfig = allPaymentConfigs.find(c => c.id === id);
    renderPaymentConfig();
}

async function setPaymentConfigDefault(id) {
    try {
        const response = await fetch(`/api/backoffice/payment/${id}/active`, {
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': getCsrfToken()
            }
        });

        if (response.ok) {
            showToast('Default payment configuration updated!');
            await loadData();
            renderPaymentConfig();
        } else {
            showToast('Error setting default configuration', 'error');
        }
    } catch (error) {
        console.error('Error setting default payment config:', error);
        showToast('Error setting default payment config', 'error');
    }
}

async function deletePaymentConfig(id) {
    if (id === 'default') return;
    if (!confirm(`Are you sure you want to delete the configuration "${id}"?`)) return;

    try {
        const response = await fetch(`/api/backoffice/payment/${id}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': getCsrfToken()
            }
        });

        if (response.ok) {
            showToast('Configuration deleted successfully!');
            await loadData();
            // Reset selection if deleted
            if (selectedPaymentConfig && selectedPaymentConfig.id === id) {
                selectedPaymentConfig = allPaymentConfigs[0];
            }
            renderPaymentConfig();
        } else {
            showToast('Error deleting configuration', 'error');
        }
    } catch (error) {
        console.error('Error deleting payment config:', error);
        showToast('Error deleting payment config', 'error');
    }
}

function createNewPaymentConfig() {
    selectedPaymentConfig = {
        id: 'new',
        activeModes: [],
        moneyDenominations: [],
        active: false
    };
    renderPaymentConfig();
}

function addDenomination() {
    const value = document.getElementById('new-denom-value').value;
    const type = document.getElementById('new-denom-type').value;
    const image = document.getElementById('new-denom-image').value;

    if (!value || !image) {
        showToast('Please provide value and image for the denomination', 'warning');
        return;
    }

    if (!selectedPaymentConfig.moneyDenominations) selectedPaymentConfig.moneyDenominations = [];

    selectedPaymentConfig.moneyDenominations.push({
        value: parseFloat(value),
        type: type,
        image: image
    });

    // Re-render to show updated list
    renderPaymentConfig();
}

function removeDenomination(index) {
    selectedPaymentConfig.moneyDenominations.splice(index, 1);
    renderPaymentConfig();
}

let denomImageCallback = null;

function openDenomImageSelector() {
    openImageSelector((url) => {
        document.getElementById('new-denom-image').value = url;
    });
}

// Override cancelForm to handle table list visibility
const originalCancelForm = cancelForm;
window.cancelForm = function () {
    const tableList = document.getElementById('tables-list');
    const tableFormContainer = document.getElementById('table-form-container');
    if (tableList) tableList.style.display = 'block';
    if (tableFormContainer) tableFormContainer.innerHTML = '';
    originalCancelForm();
};

