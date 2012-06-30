var Key = {
    ENTER: 13
};

(function ($) {
    $(function () {
        var repo = new FilteredRepository();
        new SearchBox({model: repo});
        new ReportList({model: repo});
        new CountView({model: repo});
    });
})(jQuery);


var Report = Backbone.Model.extend({
    defaults: {
        outputFormat: "html"
    },

    url: function () {
        return "rest_v2/reports" + this.get("URIString") + '.' + this.get("outputFormat");
    }
});

var Repository = Backbone.Collection.extend({
    model: Report,
    url: "rest_v2/search"
});

var FilteredRepository = Backbone.Model.extend({
    defaults: {
        repository: new Repository(),
        query: "",
        start: 0,
        max:20
    },

    url: function() {
        return "rest_v2/search?" + $.param({
            query: this.get("query"),
            start: this.get("start"),
            max: this.get("max")
        });
    },

    fetch: function() {
        var repository = this.get("repository");
        repository.url = this.url();

        var self = this;
        repository.fetch({success: function(collection) {
            self.trigger("change", collection);
        }});
    },

    resultSet: function() {
        return this.get("repository");
    }
});

var SearchBox = Backbone.View.extend({
    el: "#searchBox",

    events: {
        "keyup": "search"
    },

    render: function() {
        this.$el.val(this.model.get("query"));
        return this;
    },

    search: function(e) {
        if (e.keyCode === Key.ENTER) {
            this.model.set("query", this.$el.val());
            this.model.fetch();
        }
    }
});

var ReportList = Backbone.View.extend({
    el: "#reportsList",

    template: "#tmpl-reports",

    events: {
        "click li": "showReport"
    },

    initialize: function() {
        this.model.bind('reset', this.render, this);
        this.model.bind('change', this.render, this);
    },

    render: function() {
        var markup = _.template($(this.template).html(), {
            reports : this.model.resultSet()
        });

        this.$el.html(markup);
        return this;
    },

    showReport: function(event) {
        var id = event.target.id;
        var report = this.model.resultSet().getByCid(id);

        new ReportView({model: report}).render();
    }
});

var ReportView = Backbone.View.extend({
    el: "#viewer",

    initialize: function() {
        this.model.bind('change', this.render, this);
    },

    render: function() {
        this.changeFrameUrl(this.model.url());
        return this;
    },

    changeFrameUrl : function(url) {
        this.el.src = url;
    }
});

var CountView = Backbone.View.extend({
    el: "#reportsCount",

    initialize: function() {
        this.model.bind('change', this.render, this);
    },

    render: function() {
        this.updateCount(this.model.resultSet().size());
        return this;
    },

    updateCount : function(number) {
        this.$el.html(number);
    }
});
