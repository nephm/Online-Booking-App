let bookingToDelete = null;

function deleteBooking(bookingId) {
    bookingToDelete = bookingId;
    document.getElementById('deleteModal').style.display = 'flex';
}

document.getElementById('confirmDelete').addEventListener('click', function() {
    if (bookingToDelete) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = `/admin/delete/${bookingToDelete}`;
        
        const csrfToken = document.querySelector('meta[name="_csrf"]');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
        
        if (csrfToken && csrfHeader) {
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = csrfToken.getAttribute('content');
            form.appendChild(csrfInput);
        }

        document.body.appendChild(form);
        form.submit();
    }

    document.getElementById('deleteModal').style.display = 'none';
    bookingToDelete = null;
});

document.getElementById('cancelDelete').addEventListener('click', function() {
    document.getElementById('deleteModal').style.display = 'none';
    bookingToDelete = null;
});

document.getElementById('deleteModal').addEventListener('click', function(e) {
    if (e.target === this) {
        this.style.display = 'none';
        bookingToDelete = null;
    }
});