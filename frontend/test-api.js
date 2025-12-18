import axios from 'axios';

const testRegister = async () => {
    try {
        const response = await axios.post('http://localhost:8080/register', {
            userId: 'testnode',
            password: 'password123',
            name: 'Node Test'
        });
        console.log('Registration Success:', response.data);
    } catch (error) {
        if (error.response) {
            console.error('Registration Failed:', error.response.status, error.response.data);
        } else {
            console.error('Registration Error:', error.message);
        }
    }
};

testRegister();
