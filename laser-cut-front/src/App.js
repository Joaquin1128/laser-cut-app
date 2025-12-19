import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import { CartProvider } from './context/CartContext';
import { AuthProvider } from './context/AuthContext';
import UploadPage from './components/UploadPage';
import Wizard from './components/Wizard';
import QuotePage from './components/QuotePage';
import CartPage from './components/CartPage';
import OrdersPage from './components/OrdersPage';

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
              <Route path="/orders" element={<OrdersPage />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </div>
        </CartProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
