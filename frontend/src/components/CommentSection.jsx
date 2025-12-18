import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import './CommentSection.css';

const CommentSection = ({ postId }) => {
    const [comments, setComments] = useState([]);
    const [newComment, setNewComment] = useState('');
    const [editingCommentId, setEditingCommentId] = useState(null);
    const [editContent, setEditContent] = useState('');
    const [loading, setLoading] = useState(false);
    const [showComments, setShowComments] = useState(false);
    const { user } = useAuth();

    const fetchComments = async () => {
        setLoading(true);
        try {
            const response = await api.get(`/posts/${postId}/comments`);
            setComments(response.data);
        } catch (error) {
            console.error("Failed to fetch comments", error);
        } finally {
            setLoading(false);
        }
    };

    const handleToggleComments = () => {
        if (!showComments && comments.length === 0) {
            fetchComments();
        }
        setShowComments(!showComments);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!newComment.trim()) return;

        try {
            const response = await api.post(`/posts/${postId}/comments`, { content: newComment });
            setComments([...comments, response.data]);
            setNewComment('');
        } catch (error) {
            console.error("Failed to post comment", error);
            alert("Failed to post comment.");
        }
    };

    const handleDelete = async (commentId) => {
        if (!window.confirm("Delete this comment?")) return;
        try {
            await api.delete(`/comments/${commentId}`);
            setComments(comments.filter(c => c.id !== commentId));
        } catch (error) {
            console.error("Failed to delete comment", error);
        }
    };

    const handleEditClick = (comment) => {
        setEditingCommentId(comment.id);
        setEditContent(comment.content);
    };

    const handleCancelEdit = () => {
        setEditingCommentId(null);
        setEditContent('');
    };

    const handleUpdate = async (commentId) => {
        if (!editContent.trim()) return;
        try {
            const response = await api.put(`/comments/${commentId}`, { content: editContent });
            setComments(comments.map(c => c.id === commentId ? response.data : c));
            setEditingCommentId(null);
            setEditContent('');
        } catch (error) {
            console.error("Failed to update comment", error);
            alert("Failed to update comment.");
        }
    };

    return (
        <div className="comment-section">
            <button className="toggle-comments-btn" onClick={handleToggleComments}>
                {showComments ? "Hide Comments" : "Show Comments"}
            </button>

            {showComments && (
                <div className="comments-container">
                    {loading ? (
                        <p>Loading comments...</p>
                    ) : (
                        <>
                            <ul className="comment-list">
                                {comments.map(comment => (
                                    <li key={comment.id} className="comment-item">
                                        <div className="comment-header">
                                            <span className="comment-author">{comment.authorName}</span>
                                            <span className="comment-date">
                                                {new Date(comment.createdAt).toLocaleDateString()}
                                            </span>
                                        </div>
                                        {editingCommentId === comment.id ? (
                                            <div className="edit-comment-form">
                                                <input
                                                    type="text"
                                                    value={editContent}
                                                    onChange={(e) => setEditContent(e.target.value)}
                                                    className="edit-comment-input"
                                                />
                                                <div className="edit-actions">
                                                    <button onClick={() => handleUpdate(comment.id)} className="save-btn">Save</button>
                                                    <button onClick={handleCancelEdit} className="cancel-btn">Cancel</button>
                                                </div>
                                            </div>
                                        ) : (
                                            <p className="comment-content">{comment.content}</p>
                                        )}
                                        {user && user.name === comment.authorName && editingCommentId !== comment.id && (
                                            <div className="comment-actions">
                                                <button 
                                                    className="edit-comment-btn" 
                                                    onClick={() => handleEditClick(comment)}
                                                >
                                                    Edit
                                                </button>
                                                <button 
                                                    className="delete-comment-btn" 
                                                    onClick={() => handleDelete(comment.id)}
                                                >
                                                    Delete
                                                </button>
                                            </div>
                                        )}
                                    </li>
                                ))}
                            </ul>
                            
                            {user && (
                                <form onSubmit={handleSubmit} className="comment-form">
                                    <input
                                        type="text"
                                        value={newComment}
                                        onChange={(e) => setNewComment(e.target.value)}
                                        placeholder="Write a comment..."
                                        className="comment-input"
                                    />
                                    <button type="submit" className="comment-submit-btn">Post</button>
                                </form>
                            )}
                        </>
                    )}
                </div>
            )}
        </div>
    );
};

export default CommentSection;
