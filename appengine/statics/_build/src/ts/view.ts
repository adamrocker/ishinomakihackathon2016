export class View {
    private static _instance: View;
    private _elements: Object;
    constructor(singleton: Function) {
        if (singleton != View.__singleton) {
            throw new Error("MUST NOT BE HERE!");
        }
        this._elements = {};
    }
    private static __singleton(): void {
        // Used only for a new instance 
    }

    public static getInstance(): View {
        if (!View._instance) {
            View._instance = new View(View.__singleton);
        }
        return View._instance;
    }
}