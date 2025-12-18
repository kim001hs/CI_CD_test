import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../api/axios';
import './CreatePost.css'; // Reuse CSS

const UpdatePost = () => {
    const { id } = useParams();
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [imageUrl, setImageUrl] = useState('');
    const [image, setImage] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const [isUpdating, setIsUpdating] = useState(false);

    useEffect(() => {
        const fetchPost = async () => {
            try {
                const response = await api.get(`/posts/${id}`);
                setTitle(response.data.title);
                setContent(response.data.content);
                setImageUrl(response.data.imageUrl || '');
                setImagePreview(response.data.imageUrl || null);
            } catch (err) {
                console.error("Failed to fetch post", err);
                setError("Failed to load post data.");
            }
        };
        fetchPost();
    }, [id]);

    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setImage(file);
            const reader = new FileReader();
            reader.onloadend = () => {
                setImagePreview(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        // 1. Trigger Spin Animation
        setIsUpdating(true);

        // 2. Wait for animation (0.8s) then execute update
        setTimeout(async () => {
            try {
                let finalImageUrl = imageUrl;

                // 새 이미지가 있으면 업로드
                if (image) {
                    const formData = new FormData();
                    formData.append('file', image);

                    const uploadResponse = await api.post('/files/upload', formData, {
                        headers: {
                            'Content-Type': 'multipart/form-data',
                        },
                    });
                    finalImageUrl = uploadResponse.data.fileUrl;
                }

                await api.put(`/posts/${id}`, {
                    title,
                    content,
                    imageUrl: finalImageUrl
                });
                navigate('/');
            } catch (err) {
                console.error("Failed to update post", err);
                setError("Failed to update post. Please try again.");
                setIsUpdating(false);
            }
        }, 800);
    };

    return (
        <div className={`create-post-container ${isUpdating ? 'spinning' : ''}`}>
            <h2>Update Post</h2>
            {error && <p className="error-message">{error}</p>}
            <form onSubmit={handleSubmit} className="create-post-form">
                <div className="form-group">
                    <label htmlFor="title">Title</label>
                    <input
                        type="text"
                        id="title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="content">Content</label>
                    <textarea
                        id="content"
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        required
                        rows="5"
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="image">Image (Optional)</label>
                    <input
                        type="file"
                        id="image"
                        accept="image/*"
                        onChange={handleImageChange}
                    />
                    {imagePreview && (
                        <div className="image-preview">
                            <img src={imagePreview} alt="Preview" />
                        </div>
                    )}
                </div>
                <button type="submit" className="submit-btn" disabled={isUpdating}>
                    {isUpdating ? 'Spinning...' : 'Update Post'}
                </button>
            </form>
        </div>
    );
};

export default UpdatePost;
