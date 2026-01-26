import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import { CartProvider } from './context/CartContext';
import { AuthProvider } from './context/AuthContext';
import UploadPage from './features/upload/components/UploadPage';
import Wizard from './features/quote/components/Wizard';
import QuotePage from './features/quote/components/QuotePage';
import CartPage from './features/cart/components/CartPage';
import CheckoutPage from './features/checkout/components/CheckoutPage';
import OrdersPage from './features/orders/components/OrdersPage';
import PaymentResultPage from './features/payment/components/PaymentResultPage';

function App() {
  return (
    <Router>
      <AuthProvider>
        <CartProvider>
          <div className="App">
            <Routes>
              <Route path="/" element={<UploadPage />} />
              <Route path="/wizard" element={<Wizard />} />
              <Route path="/quote" element={<QuotePage />} />
              <Route path="/cart" element={<CartPage />} />
              <Route path="/checkout" element={<CheckoutPage />} />
              <Route path="/orders" element={<OrdersPage />} />
              <Route path="/payment/success" element={<PaymentResultPage />} />
              <Route path="/payment/failure" element={<PaymentResultPage />} />
              <Route path="/payment/pending" element={<PaymentResultPage />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </div>
        </CartProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
