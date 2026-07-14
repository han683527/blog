import axios from "axios";


const request = axios.create({
    baseURL: '/',
    timeout: 15000,
})

request.interceptors.response.use(
    (response) => {
        const res = response.data;
        if (res.code !== 200) {
            return Promise.reject(new Error(res.message))
        }
        return response
    },
    (error) => {
        return Promise.reject(error)
    })

export default request;