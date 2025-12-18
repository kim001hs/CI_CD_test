import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import CommentSection from '../components/CommentSection';
import './Home.css';

const Home = () => {
    const [posts, setPosts] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [deletingId, setDeletingId] = useState(null); // ID of post being exploded
    
    const navigate = useNavigate();
    const location = useLocation();
    const { user } = useAuth();
    const isNewPost = new URLSearchParams(location.search).get('new') === 'true';

    useEffect(() => {
        fetchPosts(page);
    }, [page]);

    // Clear "new" param after animation to prevent re-trigger on refresh
    useEffect(() => {
        if (isNewPost) {
            const timer = setTimeout(() => {
                navigate('/', { replace: true });
            }, 2000);
            return () => clearTimeout(timer);
        }
    }, [isNewPost, navigate]);

    const fetchPosts = async (pageNumber) => {
        try {
            const response = await api.get(`/posts?page=${pageNumber}&size=6`);
            setPosts(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (err) {
            console.error("Failed to fetch posts", err);
            setError("Failed to load posts.");
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (postId) => {
        if (!window.confirm("Are you sure you want to DESTROY this post?")) return;
        
        // 1. Trigger Explosion Animation
        setDeletingId(postId);

        // 2. Wait for animation (1s)
        setTimeout(async () => {
             try {
                await api.delete(`/posts/${postId}`);
                setDeletingId(null);
                fetchPosts(page); 
            } catch (err) {
                console.error("Failed to delete post", err);
                alert("Failed to delete post. You might not be the author.");
                setDeletingId(null);
            }
        }, 1000);
    };



    const handlePrevPage = () => {
        if (page > 0) setPage(page - 1);
    };

    const handleNextPage = () => {
        if (page < totalPages - 1) setPage(page + 1);
    };

    if (loading) return <div>Loading posts...</div>;

    return (
        <div className="home-container">
            <div className="home-header">
                <h2>Post List</h2>
                <button className="create-btn" onClick={() => navigate('/create-post')}>
                    Create New Post
                </button>
            </div>
            
            {error && <p className="error-message">{error}</p>}
            
            <div className="post-list">
                {posts.length === 0 ? (
                    <p>No posts available.</p>
                ) : (
                    posts.map((post, index) => {
                        // Determine classes
                        let cardClass = "post-card";
                        if (deletingId === post.id) cardClass += " exploding";
                        if (deletingId === post.id) cardClass += " exploding";
                        // Apply entrance only to the FIRST post if ?new=true
                        if (isNewPost && index === 0 && page === 0) cardClass += " special-entrance";

                        return (
                            <div key={post.id} className={cardClass}>
                                <h3>{post.title}</h3>
                                {post.imageUrl && (
                                    <div className="post-image">
                                        <img src={post.imageUrl} alt={post.title} />
                                    </div>
                                )}
                                <p className="post-content">{post.content}</p>
                                <div className="post-meta">
                                    <span className="post-author">By: {post.authorName}</span>
                                    <span className="post-date">
                                        {new Date(post.createdAt).toLocaleDateString()}
                                    </span>
                                </div>
                                {user && user.userId === post.authorUserId && (
                                    <div className="post-actions">
                                        <button className="edit-btn" onClick={() => navigate(`/update-post/${post.id}`)}>
                                            Edit
                                        </button>
                                        <button className="delete-btn" onClick={() => handleDelete(post.id)}>
                                            Delete
                                        </button>
                                    </div>
                                )}
                                <CommentSection postId={post.id} />
                            </div>
                        );
                    })
                )}
            </div>
            
            <div className="pagination">
                <button 
                    onClick={handlePrevPage} 
                    disabled={page === 0}
                    className="pagination-btn"
                >
                    Previous
                </button>
                <span className="page-info">
                    Page {page + 1} of {totalPages === 0 ? 1 : totalPages}
                </span>
                <button 
                    onClick={handleNextPage} 
                    disabled={page >= totalPages - 1}
                    className="pagination-btn"
                >
                    Next
                </button>
            </div>
        </div>
    );
};

export default Home;
