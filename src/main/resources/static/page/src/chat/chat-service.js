class ChatService {

    async loginUser(useCache) {

        if (!useCache || !this.user) {

            // login user
            await $.get('/admin/user/loginUser', (data) => {
                if (data.success) {
                    this.user = data.data;
                }
            });
        }

        return this.user;
    }

    async listUsers(useCache) {

        if (!useCache || !this.users) {

            // users
            await $.get('/admin/user/all', {
                // enabled: true
            }, (data) => {
                if (data.success) {
                    this.users = data.data;
                }
            });
        }

        return this.users;
    }

    async listChannels(useCache) {

        if (!useCache || !this.channels) {

            // channels
            await $.get('/admin/channel/listMy', (data) => {
                if (data.success) {
                    this.channels = data.data;
                }
            });

        }

        return this.channels;
    }
}

export default new ChatService();
