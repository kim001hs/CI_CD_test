import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import './Profile.css';

const Profile = () => {
    const { user, loading } = useAuth();
    const [name, setName] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');

    useEffect(() => {
        if (user) {
            setName(user.name);
        }
    }, [user]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
        try {
            const updateData = {
                userId: user.userId,
                name: name,
                password: password ? password : undefined 
            };
            
            await api.put('/me', { ...updateData, password: password || user.password }); 
            
            setMessage('Profile updated successfully!');
            setPassword('');
        } catch (err) {
             console.error(err);
             setMessage('Failed to update profile.');
        }
    };

    if (loading) return <div>Loading...</div>;

    return (
        <div className="profile-container">
            <h2>My Profile</h2>
            <p><strong>User ID:</strong> {user?.userId}</p>
            {message && <p className="success-message">{message}</p>}
            <form onSubmit={handleSubmit} className="profile-form">
                <div className="form-group">
                    <label htmlFor="name">Name</label>
                    <input
                        type="text"
                        id="name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="password">New Password (leave blank to keep)</label>
                    <input
                        type="password"
                        id="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>
                <button type="submit" className="profile-btn">Update Profile</button>
            </form>
        </div>
    );
};

export default Profile;
