import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import './CreatePost.css';

const CreatePost = () => {
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [image, setImage] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setImage(file);
            // 이미지 미리보기
            const reader = new FileReader();
            reader.onloadend = () => {
                setImagePreview(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setUploading(true);
        try {
            let imageUrl = null;

            // 이미지가 있으면 먼저 업로드
            if (image) {
                const formData = new FormData();
                formData.append('file', image);

                const uploadResponse = await api.post('/files/upload', formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                });
                imageUrl = uploadResponse.data.fileUrl;
            }

            // 게시글 생성
            await api.post('/posts', {
                title,
                content,
                imageUrl
            });
            navigate('/?new=true');
        } catch (err) {
            console.error("Failed to create post", err);
            setError("Failed to create post. Please try again.");
        } finally {
            setUploading(false);
        }
    };

    return (
        <div className="create-post-container">
            <h2>Create New Post</h2>
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
                <button type="submit" className="submit-btn" disabled={uploading}>
                    {uploading ? 'Uploading...' : 'Create Post'}
                </button>
            </form>
        </div>
    );
};

export default CreatePost;
