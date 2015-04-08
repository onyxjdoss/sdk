import Ember from "ember";

export default Ember.Controller.extend({
    model: [1,2,3],

        function () {
            gitana.connect({
                "clientKey": "54f27409-d728-4093-b16a-f6f42d8dd6ff",
                "clientSecret": "qJ3xN9d/WmGsgofnSp2KHrE9lKbnpWP1jlA/HdZL9meBzjVQNZSBsWjxePeURJw4/7I/VWAHEdQOCDc+WhW+Imuni9oyEl0xjVk++LIjpZ0=",
                "username": "44d5859a-c495-4d80-9dec-1cbccccdf971",
                "password": "h6+0nyZWAIqBiBu/+4oUcu8XZt8FJCNZFoyptUoVd6bLGTQaNkbOQltWISrNTrHHB/h6SOYyeLuIyHfB3njSOJpupX8425oTtkdDqYFOlC4=",
                "baseURL": "https://api.cloudcms.com",
                "application": "f9f39a0298514e087461"
            }, function(err) {
                if (err) {
                    return;
                }
                console.log('gitana:', this);

                // "content" is a repository allocated to this Cloud CMS application's stack
                gitana.datastore("content")
                .trap(function(err) {
                    // if anything downstream on this chain throws, this trap function will catch it
                    // notify the user through a notification.
                    self.notification = "Could not connect to CloudCMS: " + err.message;
                    $scope.$apply();
                    return false;
                })
                .readBranch("master")
                .queryNodes(query).then(function () {
                    // nodes
                    //self.nodes = this.asArray();
                    console.log(this.asArray());
                    // clear the loading notification
                });
        });
    }
});
